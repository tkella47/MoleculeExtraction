import java.math.*;
import java.util.*;
import java.util.stream.*;
import java.io.*; 
import java.util.*;
public class ECharge {
    public static final String[] ACIDS = {"AlCl3","AlH3","AlCF","AlMe3","AlEtCl2","BH3","BF3","BCl3","BBr3","BCF","BI3","SO2"};
    public static int[] acidL = {4,4,34,13,10,4,4,4,4,34,4,3};
    public static HashMap<String,Integer> acidLU = new HashMap<String,Integer>();
    
    /**
     * private class for atoms 
     * @author tommy
     *
     */
    private class atom {
        private String s = "";
        private int[] con;
        private boolean acid = false;
        
        public atom(String symbol,List<Integer> connect) {
            this.s = symbol;
            if(connect.isEmpty()) {
                this.con = null;
            } else {
                this.con = connect.stream().mapToInt(i->i).toArray();
            }
        }
        public void setAcid(boolean condition) {
            this.acid = condition;
        }
    }
    public void generateLU() {
        for(int i = 0; i < ACIDS.length; i++) {
            acidLU.put(ACIDS[i],acidL[i]);
        }
    }
    public static void print(Object s) {
        System.out.println(s);
    }
    /**
     * genAtoms pulls from the gjf file and orders the atoms and sets up connectivity
     * @param acid the acid name
     * @param number the number 
     * @return an array of connectivity of the atoms.
     * @throws Exception
     */
    public atom[] genAtoms(String name) throws Exception {
        String debugName = "11_AlEtCl2";
        
        //grab file
        File file = null;
        try {
        file = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\" + name + "_Opt_uAPFD_6311gdp_Vac_hir_nbo.gjf");
        } catch(IllegalArgumentException e) {
            System.out.println("GJF files have not been imported yet");
        }
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        
        //Lets generate our list of atoms 
        List<String[]> rawExtract = new LinkedList<String[]>();
        String st; 
        int count = 0;
        int connectStart = 0;
        while((st = br.readLine()) != null) {
            if(count > 7) {
                //print(st);
                rawExtract.add(st.split(" "));
            }
            count++;
        }
        
        // so rawExtract(x)[1] will be the atom
        List<String> atomList  = new LinkedList<String>();
        //print(rawExtract.get(0).length);
        for(int i = 0; i < rawExtract.size(); i++) {
            if(rawExtract.get(i).length == 1) {
                connectStart = i + 1;
                break;
            } else {
                atomList.add(rawExtract.get(i)[1]);
            }
        }
        String[] atoms = new String[atomList.size()];
        atoms = atomList.toArray(atoms);
        atom[] result = new atom[atoms.length];
        // the list of atom symbols have been created, now extracting connectivity information;
        for(int i = connectStart; i < rawExtract.size() - 1; i++) {
            List<Integer> connect = new LinkedList<Integer>();
            if(rawExtract.get(i).length > 2) {
                for(int j = 2; j < rawExtract.get(i).length; j+= 2) {
                    connect.add(Integer.parseInt(rawExtract.get(i)[j]));
                }
            }
            result[i - connectStart] = new atom(atoms[i - connectStart ],connect);
        }
        /*for(int i = 0 ; i < result.length; i++) {
            print("atom # is " + i + " and is " + result[i].s + " and the size of connectivity is " + (result[i].con == null ? "0" : result[i].con.length));
            
        } */
        return result;
    }
    /**
     * extracts charge given the acid name and structure
     * @param acid
     * @param name
     * @param struc
     * @return
     * @throws Exception
     */
    public Double getCharge(String acid, String name, atom[] struc) throws Exception {
        LinkedList<Double> atomDoub = new LinkedList<Double>();
        String[] array = null;
        File file = null;
        int npaCount = 0; 
        try {
            file = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\"+ name + "_Opt_uAPFD_6311gdp_Vac_hir_nbo.log");
        } catch (IllegalArgumentException e) {
            print("Files are not imported yet");
        }
        //print("here");
        BufferedReader br = new BufferedReader(new FileReader(file)); 
        String st; 
        int successCount = 4;

        while ((st = br.readLine()) != null) {
            if(st.contains("Summary of Natural Population Analysis:")) {
                npaCount++;        
            }
        
            if(npaCount == successCount) {
                //print("success");
                npaCount++;
                int lineCount = 0;
                int count = 0;
                while((st = br.readLine()) != null) {
                    lineCount++;
                    if(st.contains("===")) {
                        break;
                    }
                    if(lineCount > 5) {
                        String[] tempArray = st.split(" ");
                        array = removeEmpty(tempArray);
                        //print(st + " and array is " + array.length + " long");
                        //double is array[2];
                        atomDoub.add(Double.parseDouble(array[2]));
                        count++;
                    }
                }
               
            }
            
        }
        double result = 0;
        for(int i = 0; i < struc.length; i++) {
            if(struc[i].acid) {
                //print("acid detected");
                result += atomDoub.get(i);
            }
        }
        
        //print(result);
        return result;
    }
    /**
     * removes empty array spaces.
     * @param rfrom
     * @return
     */
    public String[] removeEmpty(String[] rfrom) {
        LinkedList<String> s = new LinkedList<String>();
        for(String i : rfrom) {
            if(!i.isBlank()) {
                s.add(i);
            }
        }
        String[] result = new String[s.size()];
        result = s.toArray(result);
        return result;
    }
    /**
     * main runs program
     * @param args
     * @throws Exception
     */
    
    public static void main(String[] args) throws Exception {
        // get gjf first...
        int number = 11;
        if(args.length == 1) {
            number = Integer.parseInt(args[0]);
        }
        ECharge test = new ECharge();
        HashMap<String,atom[]> molecule = new HashMap<String,atom[]>();
        for(int i = 0; i < ACIDS.length; i++) {
            String name = number + "_" + ACIDS[i];
            //print(ACIDS[i]);
            //print(name);
            atom[] struc = null;
            try {
                struc = test.genAtoms(name);
            }
            catch(Exception e) {
                throw e;
            }
            
            //set acids using connectivity data and queue to continue to set acid markers
            Queue<Integer> q = new LinkedList<Integer>();
            for(atom a : struc) {
                if (a.s.contains("B") || a.s.contains("Al")) {
                    //print("acid is set ");
                    
                    a.setAcid(true);
                    if(a.con != null) {
                        for(int j : a.con) {
                            q.add(j);
                        }
                    }

                }
                if (a.s.contains("S") && ACIDS[i] == "SO2") {
                    
                    if(a.con != null) {
                        
                        if(struc[a.con[0]].s.contains("O")) {
                            //print(true);
                            if(ACIDS[i] == "AlMe3") {
                                //print("Acid Set as " + a.s);
                            }
                            a.setAcid(true);
                            for(int j : a.con) {
                                q.add(j);
                            } 
                        }
                    }
                }
            }
            if(ACIDS[i] == "AlMe3") {
                //print(q.size());
            }
            
            while(!q.isEmpty()) {
                int acidIndex = q.remove();
                struc[acidIndex - 1].setAcid(true);
                if(struc[acidIndex - 1].con != null) {
                    for(int j : struc[acidIndex - 1].con) {
                        q.add(j);
                    }
                }
            }
            molecule.put(ACIDS[i],struc);
        }
        //alright we have all the acids generated now in a linkedlist.. now lets pull the 
        LinkedHashMap <String, Double> AcidCharge = new LinkedHashMap<String,Double>();
        for(String acid : ACIDS) {
            //print(acid);
            Double charge = test.getCharge(acid, number + "_" + acid, molecule.get(acid));
            Double truncatedCharge = BigDecimal.valueOf(charge)
                    .setScale(5, RoundingMode.HALF_UP)
                    .doubleValue();
            AcidCharge.put(acid,truncatedCharge);
            //print(charge); 
        }
        List<String[]> dataLines = new ArrayList<String[]>();
        for(Map.Entry<String, Double> chargeMap : AcidCharge.entrySet()) {
            print("Acid: " + chargeMap.getKey() + " Charge " + chargeMap.getValue());    
            dataLines.add(new String[] {chargeMap.getKey(), chargeMap.getValue().toString()} );
        }
        test.givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines);
        
        //Map<T3Action,T3State> actionMap = state.getTransitions();
        //for(Map.Entry<T3Action,T3State> action : actionMap.entrySet())
  
        
        
    }
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
    public void givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> dataLines ) throws IOException {
        File csvOutputFile = new File("C:\\Users\\tommy\\OneDrive\\Documents\\Rfile\\Output.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
              .map(this::convertToCSV)
              .forEach(pw::println);
        }
    }
    
    
    
}
