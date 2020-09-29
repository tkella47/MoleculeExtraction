//import java.io.*;
//import java.util.*; 
//import java.nio.charset.StandardCharsets; 
//import java.nio.file.*; 
import java.math.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*; 
import java.util.*;
public class ReadOut {
    public static String[] acids = {"AlCl3","AlH3","AlMe3","AlEtCl2","BH3","BF3","BCl3","BBr3","SO2"};
    public static int[] acidL = {4,4,13,10,4,4,4,4,3};
    public static HashMap<String,Integer> acidSet = new HashMap<String,Integer>();
    public static boolean debug = false;
    public List<String[]> dataLines = new ArrayList<String[]>();
    
    public ReadOut(List<String[]> dataLines) {
        this.dataLines = dataLines;
    }
    public static void printA(Object s) {
        System.out.println(s);
    }
    public static void print(Object s) {
        if(debug) {
            System.out.println(s);
        }
    }
    
    
    public static int[] pullConnectivity(int loc,String name) throws Exception {
        String atomLoc = "" + loc;
        List<String[]> test = new LinkedList<String[]>();
        File file = null;
        try {
        file = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\" + name+ "_Opt_uAPFD_6311gdp_Vac_hir_nbo.gjf");
        } catch(IllegalArgumentException e) {
            printA("GJF files have not been imported yet");
        }
        
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        String st; 
        while((st = br.readLine()) != null) {
            if(st.contains(atomLoc)) {    
                String[] tempArray = st.split(" ");
                tempArray = removeEmpty(tempArray);
                test.add(tempArray);
            }
        }
        List<String> connList = new LinkedList<String>();
        for(int i = 0; i < test.size(); i++) {
            String[] stArray = test.get(i); 
                if(stArray[0].contains(atomLoc)) {
                    for(int k = 0; k < stArray.length; k++) {
                        if(k == 0) {
                            
                        } else if(k%2 != 0) {
                            connList.add((stArray[k]));
                        }
                    }
                }
        }
        String[] tempOut = new String[connList.size()];
        tempOut = connList.toArray(tempOut);
        int[] output = new int[tempOut.length];
        for(int i = 0; i < tempOut.length; i++) {
            output[i] = Integer.parseInt(tempOut[i]);
        }
        return output;

    }
    
    public static void main(String[] args) throws Exception {
        List<String[]> dataLineTemp = new ArrayList<String[]>();
        generateAcids();
        
        String name  = "";
       
        double[] charge = new double[18];
        List<String[]> atoms = new LinkedList<String[]>();
        File file = null;
        // validate arguments;
        if(args.length == 0) {
            printA("running for files 1");
            name = "1";
        }
        if(args.length == 1) {
            name = args[0];
            printA("running for files " + name);
        } else if (args.length == 2) {
            name = args[0];
            printA("running for files " + name + " with debug");
            debug = true;
        }
        //name = "1";
        int loop = 0;
        while(loop < 9) {
            atoms.clear();
            String rename = name + "_" + acids[loop];
            /*
             * First loop through the log file.
             */
            
            int npaCount = 0;
            int metalLoc = 0;
            try {
            file = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\"+ rename + "_Opt_uAPFD_6311gdp_Vac_hir_nbo.log");
            } catch (IllegalArgumentException e) {
                printA("Files are not imported yet");
            }
            BufferedReader br = new BufferedReader(new FileReader(file)); 
            String st; 
            while ((st = br.readLine()) != null) {
                if(st.contains("Summary of Natural Population Analysis:")) {
                    npaCount++;        
                }
                if(npaCount == 4) {
                    npaCount++;
                    int lineCount = 0;
                    while((st = br.readLine()) != null) {
                        if(st.contains("===")) {
                            break;
                        }
                        lineCount++;
                        if(lineCount > 5 ) {
                            String[] array = st.split(" ");
                            String[] ary = removeEmpty(array);
                    //printArray(temp);
                            String[] valid = new String[3];
                            for(int j = 0 ; j < valid.length; j++) {
                                valid[j] = ary[j];
                            }
                            //print(valid[0]);
                            if(valid[0].equals("Al")|| valid[0].equals("B")) {
                                metalLoc = Integer.parseInt(valid[1]);
                                print("found " + valid[0] + "at " + valid[1]);
                            }
                            if(valid[0].equals("S") && acids[loop] == "SO2") {
                                metalLoc = Integer.parseInt(valid[1]);
                                print("found " + valid[0] + "at " + valid[1]);
                            }
                            atoms.add(valid);
                        }
                    }
                }
            }
            /*
             * go through the gjf file.
             */
            
            HashMap<Integer,Integer> acidNums = new HashMap<Integer,Integer>();
            Queue<Integer> q = new LinkedList<Integer>();
            q.add(metalLoc);
            while(!q.isEmpty()) {
                print(q.peek());
                acidNums.put(q.peek(),1);
                int[] tempArray = pullConnectivity(q.remove(),rename);
                for(int i = 0; i < tempArray.length; i++) {
                    for(int j : tempArray) {
                    }
                    q.add(tempArray[i]);
                }
            }
            
            //now we have the acid locations, time for math
            double acidCharge = 0;
            double baseCharge = 0;
            for(int i = 0; i< atoms.size(); i++) {
                String[] currentAtom = atoms.get(i);
                if(acidNums.get(Integer.parseInt(currentAtom[1])) != null) {
                    print("acid " + currentAtom[0]  + " " + currentAtom[1] + " " + currentAtom[2]);
                    acidCharge += Double.parseDouble(currentAtom[2]);
                    
                } else {
                    print("base " + currentAtom[0]  + " " + currentAtom[1] + " " + currentAtom[2]);
                    baseCharge += Double.parseDouble(currentAtom[2]);
                }
            }
            Double truncatedDoubleBase = BigDecimal.valueOf(baseCharge)
                    .setScale(5, RoundingMode.HALF_UP)
                    .doubleValue();
            Double truncatedDoubleAcid = BigDecimal.valueOf(acidCharge)
                    .setScale(5, RoundingMode.HALF_UP)
                    .doubleValue();
            printA("[" + acids[loop] + "] baseC= " + truncatedDoubleBase+ " acidC= " + truncatedDoubleAcid);
            if(loop == 2) {
                dataLineTemp.add(new String[] {"AlCF", "" } );
            }
            if(loop == 8 ) {
                dataLineTemp.add(new String[] {"BCF", "" } );
                dataLineTemp.add(new String[] {"BI3", "" } );
            }
            dataLineTemp.add(new String[] {acids[loop],truncatedDoubleAcid.toString()} );
            //lets use the metal location and grab it..
            
            loop++;
        }
        ReadOut temp = new ReadOut(dataLineTemp);
        temp.givenDataArray_whenConvertToCSV_thenOutputCreated();
        /*File myCSVFile = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\Output.csv"); //reference to your file here 
        String execString = "excel " + myCSVFile.getAbsolutePath();
        Runtime run = Runtime.getRuntime();
        try {
            Process pp = run.exec(execString);
        } catch(Exception e) {
            e.printStackTrace();
        } */
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
        //C:\Users\tommy\OneDrive\Documents\Rfile
        
        /*generateAcids();
        
        double[] sets = new double[18];
        boolean debug = false;
        int loop = 0;
        int acidCount = 0;
        File file;
        String rename = null;
        String acidName = "";
        boolean custom = false;
        String name = "";
        int split = 0;
        int end = Integer.MAX_VALUE;
        // arguments 
        if (args.length == 0) {
            split = 18;
        } else if(args.length == 1) {
            split= Integer.parseInt(args[0]);
        } else if(args.length == 2) {
            custom = true;
            name = args[0];
            split = Integer.parseInt(args[1]);
        } else if (args.length == 3) {
            custom = true;
            name = args[0];
            split = Integer.parseInt(args[1]);
            //if (args[2] == "y") {
                debug = true;
                
            //} else {
                //end = (Integer.parseInt(args[2]));
            
        /*} else if (args.length == 4) {
            custom = true;
            name = args[0];
            split = Integer.parseInt(args[1]);
            end = (Integer.parseInt(args[2]));
            debug =true;
            
        } else {
            throw new IllegalArgumentException();
        }
        //Read file
        while(loop < 17) {
            int acidLoc = 0;
            boolean found = false;
            int count = 0;
            List<String[]> atoms = new LinkedList<String[]>();
            file= new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\1_AlMe3_Opt_uAPFD_6311gdp_Vac_hir_nbo.log");
            if (custom) {
                acidName = acids[acidCount];
                rename = name + "_"+ acids[acidCount];
                acidCount++;
                file =new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\" + rename + "_Opt_uAPFD_6311gdp_Vac_hir_nbo.log"); 
            }
            BufferedReader br = new BufferedReader(new FileReader(file)); 
            String st; 
        //loop through file
            while ((st = br.readLine()) != null) {
                if(st.contains("Summary of Natural Population Analysis:")) {
                    count++;        
                }
            //At final charge analysis
                if (count == 4) {
                    found = true;
                    count++;
                    printDebug("success starting evaluation of " + rename ,debug);
                    int i = 0;
                    while((st = br.readLine()) != null) {
                    //charges end at ===
                        if(st.contains("===")) {
                            break;
                        }
                        i++;
                    //charges start at line 6
                        if(i > 5 ) {
                        //Not the best code, but splits string, removes empty space, then transfers the first three to an array
                            String[] ary = st.split(" ");
                            String[] temp = removeEmpty(ary);
                        //printArray(temp);
                            String[] valid = new String[3];
                            for (int j = 0; j < valid.length; j++) {
                                
                                valid[j] = temp[j];
                                if(valid[0] == "Al" || valid[0] == "B" || valid[0] == "S") {
                                    acidLoc = Integer.parseInt(valid[1]);
                                }
                            }
                        //print("before: " + atoms.size());
                            atoms.add(valid);
                        //print("after: " + atoms.size());  
                        }
                    }   
                }
            }
            
            if (found) {
        //do math
                String[] valid;
                double sumBase = 0;
                double sumAcid = 0;
                end  = split + acidSet.get(acidName);
                for (int i = 0; i < atoms.size(); i++) {
                    valid = atoms.get(i);
                    // if i equals the atoms nums -1
                    
                    if (i + 1 >= split  && i < end -1 ) {
                        printDebug("acid",debug);
                        if(debug) {
                            printArray(valid);
                        }
                        sumAcid += Double.parseDouble(valid[2]);    
                    } else {
                        printDebug("base",debug);
                        if(debug) {
                            printArray(valid);
                        }
                        sumBase += Double.parseDouble(valid[2]);
                    }
                }  
                Double truncatedDoubleBase = BigDecimal.valueOf(sumBase)
                .setScale(5, RoundingMode.HALF_UP)
                .doubleValue();
                Double truncatedDoubleAcid = BigDecimal.valueOf(sumAcid)
                .setScale(5, RoundingMode.HALF_UP)
                .doubleValue();
                sets[loop] = truncatedDoubleBase;
                sets[loop + 1] = truncatedDoubleAcid;
            }
            loop += 2;
        }
        int temp = 0;
        for(int i = 0; i < acids.length; i++){    
            print("AcidName: " + acids[i] + " Base: " + sets[temp] + " Acid: " + sets[temp+1]);
            temp += 2;
        }
        //print("Base: " + truncatedDoubleBase + " Acid: " + truncatedDoubleAcid);
    }
    */
    public static String removeSpace(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++){
            char c = str.charAt(i);        
            if(c!=' ') {
                result += c;
            }
        }
        return result;
    }
    /*
    public static void printDebug(Object s, boolean debug) {
        if(debug) {
            print(s);
        }
    }
    public static void print(Object s) {
        System.out.println(s);
    }
    public static void printArray(String[] array) {
        String st = "[";
        for(int i = 0; i < array.length - 1; i++) {
            st += "" + array[i] + ",";
        }
        st += "" + array[array.length - 1] + "]";
        print(st);
    } */
    public static String[] removeEmpty(String[] array) {
        List<String> valid = new LinkedList<String>();
        for(int i = 0; i < array.length; i++) {
            if(removeSpace(array[i]) != "") {
                valid.add(removeSpace(array[i]));
            }
        }
        String[] output = new String[valid.size()];
        valid.toArray(output);
        return output;
        
    } /*
    public static int[] getAcidConnect(int acidLoc, String acid, String name) throws Exception {
        File file;
        int[] acidNums = new int[acidSet.get(acid)];
        List<String[]> atoms = new LinkedList<String[]>();
        
        file =new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\1_AlCl3_Opt_uAPFD_6311gdp_Vac_hir_nbo.gjf"); 
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        String st; 
        return acidNums;
    }
    */
    
    public String convertToCSV(String[] data) {
        return Stream.of(data)
          .map(this::escapeSpecialCharacters)
          .collect(Collectors.joining(","));
    }
    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
    public void givenDataArray_whenConvertToCSV_thenOutputCreated() throws IOException {
        File csvOutputFile = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\Output.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
              .map(this::convertToCSV)
              .forEach(pw::println);
        }
    }
    
    public static void generateAcids() {
        acidSet.put("AlCl3",4);
        acidSet.put("AlH3",4);
        acidSet.put("AlMe3",13);
        acidSet.put("AlEtCl2",10);
        acidSet.put("BH3",4);
        acidSet.put("BF3",4);
        acidSet.put("BCl3",4);
        acidSet.put("BBr3",4);
        acidSet.put("SO2",3);
        
        
    } 
} 


