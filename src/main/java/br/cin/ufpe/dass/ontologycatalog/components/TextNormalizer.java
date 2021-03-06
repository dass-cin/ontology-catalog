package br.cin.ufpe.dass.ontologycatalog.components;

import br.cin.ufpe.dass.ontologycatalog.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by diego on 06/06/17.
 */
@Component
public class TextNormalizer {

    private Logger log = LoggerFactory.getLogger(TextNormalizer.class);

    private final ApplicationConfig applicationProperties;

    private Vector<String> stopwords;

    public TextNormalizer(ApplicationConfig applicationProperties) {
        this.applicationProperties = applicationProperties;
        init();
    }

    public void init() {
        String stopWordFile = applicationProperties.getInitialization().getStopWordsFile();
        try {
            this.loadStopWords(stopWordFile);
        } catch (FileNotFoundException ex) {
            String msg = "Could not load stopwords file: " + stopWordFile;
            log.error(msg, ex);
        } catch (IOException ex){
            String msg = "Something wrong with reading the file: " + stopWordFile;
            log.error(msg, ex);
        }
    }

    public String basicNormalizing(String text){
        text = text.replace("_"," ");
        text = this.splitWord_atCap(text);
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        return text.replace("- ","-").replace(" -","-").replace("( ","(").replace("[ ","[").replace("  ", " ").toLowerCase();
    }

    public String advancedNormalizing(String text){
        text = this.basicNormalizing(text);
        char[] chars = text.toLowerCase().toCharArray();
        for (int i = 0; i < chars.length; i++)
            if(!Character.isLetterOrDigit(chars[i]))  chars[i] = ' ';//** + and - signs will be REMOVED here
        if(chars.length <= 0)
            return text;
        String out = removeStopwords(chars);
        return out;
    }

    private void loadStopWords (String stopWordFile) throws IOException {
        stopwords = new Vector<String>();
        BufferedReader in = new BufferedReader(new FileReader(stopWordFile));
        String line;
        while ((line = in.readLine()) != null) {
            stopwords.add(line.trim());
        }
        in.close();
    }

    private String splitWord_atCap(String input){
        char[] chars = input.toCharArray();
        ArrayList list = new ArrayList();

        for(int i=1; i < chars.length-1; i++)
        {
            //separte the string by upcase character
            boolean condition1 = Character.isUpperCase(chars[i]);
            boolean condition2 = !Character.isUpperCase(chars[i-1]);
            boolean condition3 = !Character.isUpperCase(chars[i+1]);
            boolean condition4 = !Character.isWhitespace(chars[i-1]);
            if ( condition1 && condition2 && condition3 && condition4 )
                list.add(new Integer(i));
        }
        String out = "";
        if(list.size() != 0)
        {
            Iterator it = list.iterator();
            int start = ((Integer)it.next()).intValue();
            out += input.subSequence(0, start);
            while(it.hasNext()){
                int index = ((Integer)it.next()).intValue();
                out +=  " " + input.substring(start, index);
                start = index;
            }
            out = out + " " + input.substring(start);
        }
        else
            out = input;
        return out;
    }

    private String removeStopwords(char[] chars){
        String out = "";
        if(stopwords != null ){
            StringTokenizer st = new StringTokenizer(new String(chars));
            while(st.hasMoreTokens()){
                String token = st.nextToken().trim();
                if(!stopwords.contains(token))
                    out += token + " ";
            }
            if(out.length() <= 0)
                return (new String(chars)).trim();
            return out.trim();
        }else
            return (new String(chars)).trim();
    }
}
