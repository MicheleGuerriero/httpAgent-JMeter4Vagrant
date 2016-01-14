package it.polimi.jmetertemplatefiller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import it.cloud.Configuration;

/**
 * Hello world!
 *
 */
public class App {

    private static DecimalFormat nameFormatter = nameFormatter(10);
    private static final String DEFAULT_PATH="/home/vagrant";
    
    private static DecimalFormat nameFormatter(int chars) {
        if (chars <= 0)
            throw new RuntimeException("You need at least one char.");

        String pattern = "0";
        for (int i = 0; i < chars; ++i)
            pattern += "0";

        DecimalFormat myFormatter = new DecimalFormat(pattern);
        return myFormatter;
    }
    
    public static void main(String[] args) {
        
        if(args.length>0)
            createModifiedFile(getPathToFile(args[0]+"/jmeterTestTemplate-HTTPAgent.jmx"), args[0]+"/test/", args[0]+"/test.txt", 1, "jmeterTest-HTTPAgent");
        else
            createModifiedFile(getPathToFile(DEFAULT_PATH+"/jmeterTestTemplate-HTTPAgent.jmx"), DEFAULT_PATH+"/test/", DEFAULT_PATH+"/test.txt", 1, "jmeterTest-HTTPAgent");

    }

    public static void createModifiedFile(Path baseJmx, String remotePathTest,
            String dataFile, int clients, String testName) {

        // Date date = new Date();
        // String startTime = String.valueOf(date.getTime());
        String log_table = String.format("%s/test_table.jtl", remotePathTest);
        String log_tree = String.format("%s/test_tree.jtl", remotePathTest);
        String log_aggregate = String.format("%s/test_aggregate.jtl", remotePathTest);
        String log_graph = String.format("%s/test_graph.jtl", remotePathTest);
        String log_tps = String.format("%s/test_tps.jtl", remotePathTest);




            try {
                File fXmlFile = new File(baseJmx.toString());
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                doc.getDocumentElement().normalize();

                considerDataFile(doc, Configuration.getPathToFile(dataFile), clients);
                
                XPath xpath = XPathFactory.newInstance().newXPath();
                String expression = "//ResultCollector[@guiclass='TableVisualizer']/stringProp";
                Node xpathNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
                xpathNode.setTextContent(log_table);
                expression = "//ResultCollector[@guiclass='ViewResultsFullVisualizer']/stringProp";
                xpathNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
                xpathNode.setTextContent(log_tree);
                expression = "//ResultCollector[@guiclass='StatGraphVisualizer']/stringProp";
                xpathNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
                xpathNode.setTextContent(log_aggregate);
                expression = "//ResultCollector[@guiclass='GraphVisualizer']/stringProp";
                xpathNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
                xpathNode.setTextContent(log_graph);
                expression = "//kg.apc.jmeter.vizualizers.CorrectedResultCollector/stringProp[@name='filename']";
                xpathNode = (Node) xpath.evaluate(expression, doc, XPathConstants.NODE);
                xpathNode.setTextContent(log_tps);
                
                File jmxFile = Paths.get(remotePathTest, testName + ".jmx").toFile();
                TransformerFactory transformerFactory = TransformerFactory
                        .newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                doc.setXmlStandalone(true);
                
                try {
                    jmxFile.getParentFile().mkdirs();
                } catch (Exception e) { }
                
                StreamResult result = new StreamResult(jmxFile);
                transformer.transform(source, result);
                
            } catch (XPathExpressionException e) {
                throw new RuntimeException(e.getMessage(), e.getCause());
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (TransformerConfigurationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (TransformerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


    }

    private static void considerDataFile(Document doc, Path data, int clients) throws IOException {
        final String ultimateThreadGroup = "kg.apc.jmeter.threads.UltimateThreadGroup";
        final String variableThroughputTimer = "kg.apc.jmeter.timers.VariableThroughputTimer";

        if (doc.getElementsByTagName(ultimateThreadGroup).getLength() != 1)
            throw new RuntimeException("No " + ultimateThreadGroup + " found or too many of them in the same file.");

        if (clients < 1)
            throw new RuntimeException("There should be at least one client running the JMX.");

        boolean useVariableTimer = (doc.getElementsByTagName(variableThroughputTimer).getLength() == 1);

        Node variableTimer = null;
        Element variableTimerCol = null;
        if (useVariableTimer) {
            variableTimer = (Node) doc.getElementsByTagName(variableThroughputTimer).item(0);

            while (variableTimer.hasChildNodes())
                variableTimer.removeChild(variableTimer.getFirstChild());
            variableTimer.appendChild(doc.createTextNode("\n"));
            variableTimerCol = doc.createElement("collectionProp");
            variableTimerCol.appendChild(doc.createTextNode("\n"));
            variableTimerCol.setAttribute("name", "load_profile");
            variableTimer.appendChild(variableTimerCol);
        }

        Node ultimateGroup = (Node) doc.getElementsByTagName(ultimateThreadGroup).item(0);

        // while (ultimateGroup.hasChildNodes())
        // ultimateGroup.removeChild(ultimateGroup.getFirstChild());
        ultimateGroup.appendChild(doc.createTextNode("\n"));
        Element ultimateGroupCol = doc.createElement("collectionProp");
        ultimateGroupCol.appendChild(doc.createTextNode("\n"));
        ultimateGroupCol.setAttribute("name", "ultimatethreadgroupdata");
        // ultimateGroup.appendChild(ultimateGroupCol);

        NodeList collectionProps = ((Element) ultimateGroup).getElementsByTagName("collectionProp");
        if (collectionProps.getLength() > 0) {
            boolean done = false;
            for (int i = 0; i < collectionProps.getLength() && !done; ++i)
                if (((Element) collectionProps.item(i)).getAttribute("name")
                        .equalsIgnoreCase("ultimatethreadgroupdata")) {
                    ultimateGroup.removeChild(collectionProps.item(i));
                    done = true;
                }
        }
        ultimateGroup.appendChild(ultimateGroupCol);

        int start_time = 0;
        int index = 1;

        try (Scanner input = new Scanner(data)) {
            while (input.hasNextLine()) {
                String nameProp = nameFormatter.format(index);
                String line = input.nextLine();
                String[] values = line.trim().split(" ");
                if (values.length < 2)
                    continue;

                if (useVariableTimer) {
                    Element colp = doc.createElement("collectionProp");
                    colp.appendChild(doc.createTextNode("\n"));
                    colp.setAttribute("name", nameProp);
                    variableTimerCol.appendChild(colp);

                    Element prop1 = doc.createElement("stringProp");
                    prop1.appendChild(doc.createTextNode(values[2]));
                    prop1.setAttribute("name", "1");
                    Element prop2 = doc.createElement("stringProp");
                    prop2.appendChild(doc.createTextNode(values[2]));
                    prop2.setAttribute("name", "2");
                    Element prop3 = doc.createElement("stringProp");
                    prop3.appendChild(doc.createTextNode(values[1]));
                    prop3.setAttribute("name", "3");

                    colp.appendChild(prop1);
                    colp.appendChild(doc.createTextNode("\n"));
                    colp.appendChild(prop2);
                    colp.appendChild(doc.createTextNode("\n"));
                    colp.appendChild(prop3);
                    colp.appendChild(doc.createTextNode("\n"));
                }

                {
                    int ramp = 10;
                    if (values.length >= 4)
                        ramp = Integer.valueOf(values[3]);

                    Element colp1 = doc.createElement("collectionProp");
                    colp1.appendChild(doc.createTextNode("\n"));
                    colp1.setAttribute("name", nameProp);
                    ultimateGroupCol.appendChild(colp1);

                    Element prop1 = doc.createElement("stringProp");
                    prop1.appendChild(doc.createTextNode(
                            Integer.valueOf((int) Math.ceil(Double.parseDouble(values[0]) / clients)).toString()));
                    prop1.setAttribute("name", "1");
                    Element prop2 = doc.createElement("stringProp");
                    String st = String.valueOf(start_time);
                    prop2.appendChild(doc.createTextNode(st));
                    start_time = start_time + (int) Double.parseDouble(values[1]) + (ramp * 2);
                    prop2.setAttribute("name", "2");
                    Element prop3 = doc.createElement("stringProp");
                    prop3.appendChild(doc.createTextNode(Integer.valueOf(ramp).toString()));
                    prop3.setAttribute("name", "3");
                    Element prop4 = doc.createElement("stringProp");
                    prop4.appendChild(doc.createTextNode(values[1]));
                    prop4.setAttribute("name", "4");
                    Element prop5 = doc.createElement("stringProp");
                    prop5.appendChild(doc.createTextNode(Integer.valueOf(ramp).toString()));
                    prop5.setAttribute("name", "5");

                    colp1.appendChild(prop1);
                    colp1.appendChild(doc.createTextNode("\n"));
                    colp1.appendChild(prop2);
                    colp1.appendChild(doc.createTextNode("\n"));
                    colp1.appendChild(prop3);
                    colp1.appendChild(doc.createTextNode("\n"));
                    colp1.appendChild(prop4);
                    colp1.appendChild(doc.createTextNode("\n"));
                    colp1.appendChild(prop5);
                    colp1.appendChild(doc.createTextNode("\n"));
                }

                index++;

            }
        }
    }
    
    public static Path getPathToFile(String filePath) {
        File f = new File(filePath);
        if (f.exists())
            try {
                return f.toPath();
            } catch (Exception e) { }
        
        URL url = Configuration.class.getResource(filePath);
        if (url == null)
            url = Configuration.class.getResource("/" + filePath);
        if (url == null)
            return null;
        else
            return Paths.get(url.getPath());
    }

}
