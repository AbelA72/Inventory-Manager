import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/** Reusable structured-document parser. Supports CSV and ordinary XLSX worksheets. */
public final class SalesImporter {
    public record Row(LocalDate date,String item,double quantity,double unitPrice,double total,String order,String payment,int sourceRow,List<String> errors) {}
    public record Parsed(List<String> columns,List<Map<String,String>> rawRows) {}
    private SalesImporter() {}
    public static Parsed parse(String filename,byte[] bytes)throws Exception{
        String lower=filename.toLowerCase(Locale.ROOT);
        if(lower.endsWith(".csv"))return csv(new String(bytes,StandardCharsets.UTF_8));
        if(lower.endsWith(".xlsx"))return xlsx(bytes);
        throw new IllegalArgumentException("Use a CSV or XLSX file for this MVP");
    }
    public static List<Row> normalize(Parsed p,Map<String,String> mapping){
        List<Row> out=new ArrayList<>();int line=1;
        for(Map<String,String> raw:p.rawRows()){line++;List<String> errors=new ArrayList<>();String item=value(raw,mapping,"item");String ds=value(raw,mapping,"date");double qty=num(value(raw,mapping,"quantity"),"quantity",errors);double price=num(value(raw,mapping,"unitPrice"),"unit price",new ArrayList<>());double total=num(value(raw,mapping,"total"),"total",errors);LocalDate date;
            try{date=LocalDate.parse(ds.trim());}catch(Exception e){date=null;errors.add("Invalid date");}if(item.isBlank())errors.add("Missing menu item");if(qty<=0)errors.add("Quantity must be positive");if(total<0)errors.add("Total cannot be negative");if(price==0&&qty>0)price=total/qty;if(total==0&&price>0)total=price*qty;if(price>0&&total>0&&Math.abs(price*qty-total)>.02)errors.add("Quantity × unit price does not match total");
            out.add(new Row(date,item.trim(),qty,price,total,value(raw,mapping,"order"),value(raw,mapping,"payment"),line,List.copyOf(errors)));
        }return out;
    }
    public static Map<String,String> detect(List<String> cols){Map<String,String> m=new LinkedHashMap<>();for(String c:cols){String k=c.toLowerCase().replaceAll("[^a-z]","");if(k.contains("date"))m.putIfAbsent("date",c);else if(k.contains("item")||k.contains("product")||k.contains("menu"))m.putIfAbsent("item",c);else if(k.equals("qty")||k.contains("quantity"))m.putIfAbsent("quantity",c);else if(k.contains("unitprice")||k.equals("price"))m.putIfAbsent("unitPrice",c);else if(k.contains("total")||k.contains("netsales")||k.contains("amount"))m.putIfAbsent("total",c);else if(k.contains("order"))m.putIfAbsent("order",c);else if(k.contains("payment"))m.putIfAbsent("payment",c);}return m;}
    private static String value(Map<String,String>r,Map<String,String>m,String field){return r.getOrDefault(m.getOrDefault(field,""),"").trim();}
    private static double num(String s,String name,List<String> errors){if(s.isBlank())return 0;try{return Double.parseDouble(s.replace(",","").replaceAll("[^0-9.\\-]",""));}catch(Exception e){errors.add("Invalid "+name);return 0;}}
    private static Parsed csv(String text){List<List<String>> rows=new ArrayList<>();List<String> row=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false;for(int i=0;i<text.length();i++){char c=text.charAt(i);if(c=='\"'){if(quoted&&i+1<text.length()&&text.charAt(i+1)=='\"'){cell.append('\"');i++;}else quoted=!quoted;}else if(c==','&&!quoted){row.add(cell.toString().trim());cell.setLength(0);}else if((c=='\n'||c=='\r')&&!quoted){if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;row.add(cell.toString().trim());cell.setLength(0);if(row.stream().anyMatch(s->!s.isBlank()))rows.add(row);row=new ArrayList<>();}else cell.append(c);}if(cell.length()>0||!row.isEmpty()){row.add(cell.toString().trim());rows.add(row);}return table(rows);}
    private static Parsed xlsx(byte[] bytes)throws Exception{Map<String,byte[]> files=new HashMap<>();try(ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(bytes))){for(ZipEntry e;(e=z.getNextEntry())!=null;)files.put(e.getName(),z.readAllBytes());}if(!files.containsKey("xl/worksheets/sheet1.xml"))throw new IllegalArgumentException("Workbook has no first worksheet");List<String> shared=new ArrayList<>();if(files.containsKey("xl/sharedStrings.xml")){Document d=xml(files.get("xl/sharedStrings.xml"));NodeList si=d.getElementsByTagName("si");for(int i=0;i<si.getLength();i++)shared.add(si.item(i).getTextContent());}Document d=xml(files.get("xl/worksheets/sheet1.xml"));NodeList rs=d.getElementsByTagName("row");List<List<String>> rows=new ArrayList<>();for(int i=0;i<rs.getLength();i++){Element re=(Element)rs.item(i);NodeList cs=re.getElementsByTagName("c");Map<Integer,String> cells=new TreeMap<>();for(int k=0;k<cs.getLength();k++){Element c=(Element)cs.item(k);String ref=c.getAttribute("r");int col=column(ref);NodeList vs=c.getElementsByTagName("v");String v=vs.getLength()==0?c.getTextContent():vs.item(0).getTextContent();if("s".equals(c.getAttribute("t")))v=shared.get(Integer.parseInt(v));cells.put(col,v);}int max=cells.keySet().stream().mapToInt(x->x).max().orElse(-1);List<String> row=new ArrayList<>();for(int k=0;k<=max;k++)row.add(cells.getOrDefault(k,""));rows.add(row);}return table(rows);}
    private static Document xml(byte[] b)throws Exception{var f=DocumentBuilderFactory.newInstance();f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);return f.newDocumentBuilder().parse(new ByteArrayInputStream(b));}
    private static int column(String ref){int n=0;for(char c:ref.toCharArray()){if(!Character.isLetter(c))break;n=n*26+(Character.toUpperCase(c)-'A'+1);}return n-1;}
    private static Parsed table(List<List<String>> rows){if(rows.isEmpty())throw new IllegalArgumentException("Document is empty");List<String> h=rows.get(0);List<Map<String,String>> data=new ArrayList<>();for(int i=1;i<rows.size();i++){Map<String,String> r=new LinkedHashMap<>();for(int j=0;j<h.size();j++)r.put(h.get(j),j<rows.get(i).size()?rows.get(i).get(j):"");data.add(r);}return new Parsed(List.copyOf(h),data);}
}
