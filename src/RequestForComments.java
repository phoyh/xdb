import xdb.*;

class RequestForComments {
 private static final String RFC_XSLT="../XMLStore/rfc/rfc.xsl";
 public static void main(String[] args) {
  if (args.length!=2) {
   System.out.println("Syntax: rfc <source_rfc.xml> <dest_rfc.html>");
   System.exit(0);
  }
  try {
   xmlDriver xdr=new xmlDriverImpl();
   XML source=xdr.getXML(new URI(args[0]));
   XSLT tran=xdr.getXSLT(new URI(RFC_XSLT));
   XML dest=tran.transform(source);
   dest.setURI(new URI(args[1]));
   dest.save();
  }
  catch (xdbException e) {System.out.println(e);}
 }
}