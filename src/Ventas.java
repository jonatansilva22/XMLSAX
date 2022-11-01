import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class Ventas extends DefaultHandler {

    private static final String CLASS_NAME = Ventas.class.getName();
    private final static Logger LOG = Logger.getLogger(CLASS_NAME);

    private SAXParser parser = null;
    private SAXParserFactory spf;

    private double totalSales;
    private boolean inSales;

    String currentElement;
    String id;
    String name;
    String lastName;
    String sales;
    String state;
    String dept;

    String keyword;

    private HashMap<String, Double> ventas_edo;
    private HashMap<String, Double> ventas_dpto;

    public Ventas() {
        super();
        spf = SAXParserFactory.newInstance();
        // verificar espacios de nombre
        spf.setNamespaceAware(true);
        // validar que el documento esté bien formado (well formed)
        spf.setValidating(true);

        ventas_edo = new HashMap<>();
        ventas_dpto = new HashMap<>();
    }

    private void process(File file) {
        try {
            // obtener un parser para verificar el documento
            parser = spf.newSAXParser();

        } catch (SAXException | ParserConfigurationException e) {
            LOG.severe(e.getMessage());
            System.exit(1);
        }
        System.out.println("\nStarting parsing of " + file + "\n");
        try {
            // iniciar analisis del documento
            keyword = state;
            parser.parse(file, this);
        } catch (IOException | SAXException e) {
            LOG.severe(e.getMessage());
        }
    }

    @Override
    public void startDocument() throws SAXException {
        // al inicio del documento inicializar
        // las ventas totales
        totalSales = 0.0;
    }

    @Override
    public void endDocument() throws SAXException {
        // Se proceso el documento completo, imprimir resultado
        Set<Map.Entry<String, Double>> Estado = ventas_edo.entrySet();
        Set<Map.Entry<String, Double>> Deps = ventas_dpto.entrySet();
        System.out.println("-----Ventas por Estado-----");
        for (Map.Entry<String, Double> entry : Estado) {
            System.out.printf("%-15.15s $%,9.2f\n", entry.getKey(), entry.getValue());
        }
        System.out.println("-----Ventas por Departamento-----");
        for(Map.Entry<String, Double> entry : Deps){
            System.out.printf("%-15.15s $,9.2f\n", entry.getKey(), entry.getValue());
        }
        System.out.printf("Total de ventas: $%,9.2f\n", totalSales);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        if (localName.equals("sale_record")) {
            inSales = true;
        }
        currentElement = localName;
    }

    @Override
    public void characters(char[] bytes, int start, int length) throws SAXException {
        switch (currentElement) {
            case "id":
                this.id = new String(bytes, start, length);
                break;
            case "first_name":
                this.name = new String(bytes, start, length);
                break;
            case "last_name":
                this.lastName = new String(bytes, start, length);
                break;
            case "sales":
                this.sales = new String(bytes, start, length);
                break;
            case "state":
                this.state = new String(bytes, start, length);
                break;
            case "department":
                this.dept = new String(bytes, start, length);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("sale_record")) {
            double sl = 0.0;
            try {
                sl = Double.parseDouble(this.sales);
            } catch (NumberFormatException e) {
                LOG.severe(e.getMessage());
            }
            if (ventas_edo.containsKey(this.state)) {
                double sum = ventas_edo.get(this.state);
                ventas_edo.put(this.state, sum + sl);
            } else {
                ventas_edo.put(this.state, sl);
            }
            if (ventas_dpto.containsKey(this.dept)) {
                double sum = ventas_dpto.get(this.dept);
                ventas_dpto.put(this.dept, sum + sl);
            } else {
                ventas_dpto.put(this.dept, sl);
            }
            totalSales = totalSales + sl;
            inSales = false;
        }
    }

    private void printRecord() {
        System.out.printf("%4.4s %-10.10s %-10.10s %9.9s %-10.10s %-15.15s\n",
                id, name, lastName, sales, state, dept);
    }

    public static void main(String args[]) {
        if (args.length == 0) {
            LOG.severe("No file to process. Usage is:" + "\njava DeptSalesReport <keyword>");
            return;
        }
        File xmlFile = new File(args[0]);
        Ventas handler = new Ventas();
        handler.process(xmlFile);
    }
}