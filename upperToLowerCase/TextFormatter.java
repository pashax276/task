import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextFormatter {

    /**
     * Путь к файлу
     */
    static String pathJava = "D:\\Test\\test2.jar"; //Тут был адрес файла

    static ArrayList<String> lowerCaseWords = new ArrayList<String>();
    static ArrayList<String> upperCaseWords = new ArrayList<String>();
    static int end;

    public static void main(String args[]) throws Exception {
        StringBuilder sbJava = new StringBuilder();

        BufferedReader bR = new BufferedReader(new FileReader(pathJava));
        char[] buf = new char[1000];
        int r;
        do {
            if ((r = bR.read(buf)) > 0)
                sbJava.append(new String(buf, 0, r));
        }
        while (r > 0);


        String fileOpenJar = sbJava.toString();

        Pattern patternJar = Pattern.compile(new StringBuilder().append("([.][A-Z][\w]+\((.*)\))[\^)]").toString());
        Matcher matcherJar = patternJar.matcher(fileOpenJar);

        while (matcherJar.find()) {
            upperCaseWords.add(matcherJar.group(1).replace(".", ""));
        }

        System.out.println("upperCaseWords: " + upperCaseWords);

        for (int i = 0; i < upperCaseWords.size(); i++) {

            Matcher m = Pattern.compile("([A-Z])([A-Z]*)([A-Z][a-z0-9]*)").matcher(upperCaseWords.get(i));

            while (m.find()) {
                lowerCaseWords.add(m.group(1).toLowerCase() + m.group(2).toLowerCase() + m.group(3));

                System.out.println("m.group(1)         " + m.group(1));
                System.out.println("m.group(1) length " + m.group(1).length());
                System.out.println("-----------------------------------------");
                System.out.println("m.group(2)        " + m.group(2));
                System.out.println("m.group(2) length " + m.group(2).length());
                System.out.println("-----------------------------------------");
                System.out.println("m.group(3)        " + m.group(3));
                System.out.println("m.group(3) length " + m.group(3).length());
                end = m.group(3).length();
            }

        }

        System.out.println("LowerCaseWords: " + lowerCaseWords);


        //System.out.println(sbJava);

     /* int index =0;
        for (int i = 0; i < upperCaseWords.size(); i++) {

            while ((index = sbJava.indexOf(upperCaseWords.get(i), index)) >=0) {
                sbJava.delete(index, index + upperCaseWords.size());
                sbJava.insert(index, lowerCaseWords.get(i));
                index += lowerCaseWords.size();
            }
        }*/
        System.out.println("after > ");
        System.out.println(sbJava.toString());

    }


    public static void write(String pathJava, String text) {
        try {
            PrintWriter out = new PrintWriter(new File(pathJava).getAbsoluteFile());
            try {
                out.print(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
