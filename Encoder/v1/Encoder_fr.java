// File:         Encoder.java
    // Created:      8/10/2011
    // Author:       Jason Appelbaum Jason@Hak5.org
    // Modified:     6/12/2012
    // Author:       Dnucna
     
    import java.io.DataInputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.IOException;
    import java.io.InputStream;
    import java.util.ArrayList;
    import java.util.List;
     
    import javax.swing.text.BadLocationException;
    import javax.swing.text.Document;
    import javax.swing.text.rtf.RTFEditorKit;
     
    import java.util.Properties;
     
    public class Encoder_fr {
            /* contains the keyboard configuration */
            private static Properties keyboardProps = new Properties();
            /* contains the language layout */
            private static Properties layoutProps = new Properties();
           
            public static void main(String[] args) {
                    String helpStr = "Hak5 Duck Encoder 1.3\n\n"
                            + "usage: duckencode -i [file ..]\t\t\tencode specified file\n"
                            + "   or: duckencode -i [file ..] -o [file ..]\tencode to specified file\n"
                            + "\nArguments:\n"
                            + "   -i [file ..] \t\tInput File\n"
                            + "   -o [file ..] \t\tOutput File\n"
                            + "   -l [file ..] \t\tKeyboard Layout (us/fr or a path to a properties file)\n"
                            + "\nScript Commands:\n"
                            + "   ALT [key name] (ex: ALT F4, ALT SPACE)\n"
                            + "   CTRL | CONTROL [key name] (ex: CTRL ESC)\n"
                            + "   CTRL-ALT [key name] (ex: CTRL-ALT DEL)\n"
                            + "   CTRL-SHIT [key name] (ex: CTRL-SHIFT ESC)\n"
                            + "   DEFAULT_DELAY | DEFAULTDELAY [Time in millisecond * 10] (change the delay between each command)\n"
                            + "   DELAY [Time in millisecond * 10] (used to overide temporary the default delay)\n"
                            + "   GUI | WINDOWS [key name] (ex: GUI r, GUI l)\n"
                            + "   REM [anything] (used to comment your code, no obligation :) )\n"
                            + "   SHIFT [key name] (ex: SHIFT DEL)\n"
                            + "   STRING [any character of your layout]\n"
                            + "   [key name] (anything in the keyboard.properties)";                       
     
            String inputFile = null;
            String outputFile = null;
            String layoutFile = null;
     
            if (args.length == 0) {
                    System.out.println(helpStr);
                    System.exit(0);
            }
     
            for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("--gui") || args[i].equals("-g")) {
                            System.out.println("Launch GUI");
                    } else if (args[i].equals("--help") || args[i].equals("-h")) {
                            System.out.println(helpStr);
                    } else if (args[i].equals("-i")) {
                            // encode file
                            inputFile = args[++i];
                    } else if (args[i].equals("-o")) {
                            // output file
                            outputFile = args[++i];
                    } else if (args[i].equals("-l")) {
                            // output file
                            layoutFile = args[++i];
                    } else {
                            System.out.println(helpStr);
                            break;
                    }
            }
     
            if (inputFile != null) {
                    String scriptStr = null;
     
                    if (inputFile.contains(".rtf")) {
                            try {
                                    FileInputStream stream = new FileInputStream(inputFile);
                                    RTFEditorKit kit = new RTFEditorKit();
                                    Document doc = kit.createDefaultDocument();
                                    kit.read(stream, doc, 0);
     
                                    scriptStr = doc.getText(0, doc.getLength());
                            } catch (IOException e) {
                                    System.out.println("Error with input file!");
                            } catch (BadLocationException e) {
                                    System.out.println("Error with input file!");
                            }
                    } else {
                            DataInputStream in = null;
                            try {
                                    File f = new File(inputFile);
                                    byte[] buffer = new byte[(int) f.length()];
                                    in = new DataInputStream(new FileInputStream(f));
                                    in.readFully(buffer);
                                    scriptStr = new String(buffer);
     
                            } catch (IOException e) {
                                    System.out.println("Error with input file!");
                            } finally {
                                    try {
                                            in.close();
                                    } catch (IOException e) { /* ignore it */
                                    }
                            }
                    }
                    loadProperties((layoutFile == null) ? "us" : layoutFile);
                   
                    encodeToFile(scriptStr, (outputFile == null) ? "inject.bin"
                                    : outputFile);
                    }
               
            }
           
            private static void loadProperties (String lang){
                    InputStream in;
                    ClassLoader loader = ClassLoader.getSystemClassLoader ();
                    try {
                            in = loader.getResourceAsStream("keyboard.properties");
                            if(in != null){
                                    keyboardProps.load(in);
                                    in.close();
                            }else{
                                    System.out.println("Error with keyboard.properties!");
                                    System.exit(0);
                            }
                    } catch (IOException e) {
                            System.out.println("Error with keyboard.properties!");
                    }
                           
                    try {
                            in = loader.getResourceAsStream(lang + ".properties");
                            if(in != null){
                                    layoutProps.load(in);
                                    in.close();
                            }else{
                                    if(new File(lang).isFile()){
                                            layoutProps.load(new FileInputStream(lang));
                                    } else{
                                            System.out.println("External layout.properties non found!");
                                            System.exit(0);
                                    }
                            }
                    } catch (IOException e) {
                            System.out.println("Error with layout.properties!");
                            System.exit(0);
                    }
     
            }
            private static void encodeToFile(String inStr, String fileDest) {
     
                    inStr = inStr.replaceAll("\\r", ""); // CRLF Fix
                    String[] instructions = inStr.split("\n");
                    List<Byte> file = new ArrayList<Byte>();
                    int defaultDelay = 0;
     
                    for (int i = 0; i < instructions.length; i++) {
                            try {
                                    boolean delayOverride = false;
                                    String commentCheck = instructions[i].substring(0, 2);
                                    if (commentCheck.equals("//"))
                                            continue;
     
                                    String instruction[] = instructions[i].split(" ", 2);
     
                                    instruction[0].trim();
     
                                    if (instruction.length == 2) {
                                            instruction[1].trim();
                                    }
     
                                    if (instruction[0].equals("DEFAULT_DELAY")
                                                    || instruction[0].equals("DEFAULTDELAY")) {
                                            defaultDelay = Integer.parseInt(instruction[1].trim());
                                            delayOverride = true;
                                    } else if (instruction[0].equals("DELAY")) {
                                            int delay = Integer.parseInt(instruction[1].trim());
                                            while (delay > 0) {
                                                    file.add((byte) 0x00);
                                                    if (delay > 255) {
                                                            file.add((byte) 0xFF);
                                                            delay = delay - 255;
                                                    } else {
                                                            file.add((byte) delay);
                                                            delay = 0;
                                                    }
                                            }
                                            delayOverride = true;
                                    } else if (instruction[0].equals("STRING")) {
                                            for (int j = 0; j < instruction[1].length(); j++) {
                                                    char c = instruction[1].charAt(j);
                                                    addBytes(file,charToBytes(c));
                                            }
                                    } else if (instruction[0].equals("CONTROL")
                                                    || instruction[0].equals("CTRL")) {
                                            if (instruction.length != 1){
                                                    file.add(strInstrToByte(instruction[1]));
                                                    file.add(strToByte(keyboardProps.getProperty("MODIFIER_CTRL")));
                                            } else {
                                                    file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_CTRL")));
                                                    file.add((byte) 0x00);
                                            }                              
                                    } else if (instruction[0].equals("ALT")) {
                                            if (instruction.length != 1){
                                                    file.add(strInstrToByte(instruction[1]));
                                                    file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT")));
                                            } else {
                                                    file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_ALT")));
                                                    file.add((byte) 0x00);
                                            }
                                    } else if (instruction[0].equals("SHIFT")) {
                                            if (instruction.length != 1) {
                                                    file.add(strInstrToByte(instruction[1]));
                                                    file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT")));
                                            } else {
                                                    file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_SHIFT")));
                                                    file.add((byte) 0x00);
                                            }
                                    } else if (instruction[0].equals("CTRL-ALT")) {
                                            if (instruction.length != 1) {
                                                    file.add(strInstrToByte(instruction[1]));
                                                    file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL"))
                                                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_ALT"))));
                                            } else {
                                                    continue;
                                            }
                                    } else if (instruction[0].equals("CTRL-SHIFT")) {
                                            if (instruction.length != 1) {
                                                    file.add(strInstrToByte(instruction[1]));
                                                    file.add((byte) (strToByte(keyboardProps.getProperty("MODIFIERKEY_CTRL"))
                                                                    | strToByte(keyboardProps.getProperty("MODIFIERKEY_SHIFT"))));
                                            } else {
                                                    continue;
                                            }
                                    } else if (instruction[0].equals("REM")) {
                                            /* no default delay for the comments */
                                            delayOverride = true;
                                            continue;
                                    } else if (instruction[0].equals("WINDOWS")
                                                    || instruction[0].equals("GUI")) {
                                            if (instruction.length == 1) {
                                                    file.add(strToByte(keyboardProps.getProperty("KEY_LEFT_GUI")));
                                                    file.add((byte) 0x00);
                                            } else {
                                                    file.add(strInstrToByte(instruction[1]));
                                                    file.add(strToByte(keyboardProps.getProperty("MODIFIERKEY_LEFT_GUI")));
                                            }
                                    } else {
                                            /* treat anything else as a key */
                                            file.add(strInstrToByte(instruction[0]));
                                            file.add((byte) 0x00);
                                    }
     
                                    // Default delay
                                    if (!delayOverride & defaultDelay > 0) {
                                            int delayCounter = defaultDelay;
                                            while (delayCounter > 0) {
                                                    file.add((byte) 0x00);
                                                    if (delayCounter > 255) {
                                                            file.add((byte) 0xFF);
                                                            delayCounter = delayCounter - 255;
                                                    } else {
                                                            file.add((byte) delayCounter);
                                                            delayCounter = 0;
                                                    }
                                            }
                                    }
                            } catch (Exception e) {
                                    System.out.println("Error on Line: " + (i + 1));
                                    e.printStackTrace();
                            }
                    }
     
                    // Write byte array to file
                    byte[] data = new byte[file.size()];
                    for (int i = 0; i < file.size(); i++) {
                            data[i] = file.get(i);
                    }
                    try {
                            File someFile = new File(fileDest);
                            FileOutputStream fos = new FileOutputStream(someFile);
                            fos.write(data);
                            fos.flush();
                            fos.close();
                    } catch (Exception e) {
                            System.out.print("Failed to write hex file!");
                    }
            }
     
            private static void addBytes(List<Byte> file, byte[] byteTab){
                    for(int i=0;i<byteTab.length;i++)
                            file.add(byteTab[i]);
                    if(byteTab.length % 2 != 0){
                            file.add((byte) 0x00);
                    }
            }
           
            private static byte[] charToBytes (char c){
                    return codeToBytes(charToCode(c));
            }
            private static String charToCode (char c){
                    String code;
                    if(c<128){
                    code = "ASCII_"+Integer.toHexString(c).toUpperCase();
                }else if (c<256){
                    code = "ISO_8859_1_"+Integer.toHexString(c).toUpperCase();
                }else{
                    code = "UNICODE_"+Integer.toHexString(c).toUpperCase();
                }
                    return code;
            }
           
            private static byte[] codeToBytes (String str){
                    String keys[] = layoutProps.getProperty(str).split(",");
                    byte[] byteTab = new byte[keys.length];
                for(int j=0;j<keys.length;j++){
                    String key = keys[j].trim();
                    if(keyboardProps.getProperty(key) != null){
                            byteTab[j] = strToByte(keyboardProps.getProperty(key).trim());
                    }else if(layoutProps.getProperty(key) != null){
                            byteTab[j] = strToByte(layoutProps.getProperty(key).trim());
                    }else{
                            System.out.println("Key not found !");
                            System.exit(0);
                    }
                }
                    return byteTab;
            }
            private static byte strToByte(String str) {
                    if(str.startsWith("0x")){
                            return (byte)Integer.parseInt(str.substring(2),16);
                    }else{
                            return (byte)Integer.parseInt(str);
                    }
            }
           
            private static byte strInstrToByte(String instruction){
                    instruction = instruction.trim();
                    if(keyboardProps.getProperty("KEY_"+instruction)!=null)
                            return strToByte(keyboardProps.getProperty("KEY_"+instruction));
                    /* instruction different from the key name */
                    if(instruction.equals("ESCAPE"))
                            return strInstrToByte("ESC");
                    if(instruction.equals("DEL"))
                            return strInstrToByte("DELETE");
                    if(instruction.equals("BREAK"))
                            return strInstrToByte("PAUSE");
                    if(instruction.equals("CONTROL"))
                            return strInstrToByte("CTRL");
                    if(instruction.equals("DOWNARROW"))
                            return strInstrToByte("DOWN");
                    if(instruction.equals("UPARROW"))
                            return strInstrToByte("UP");
                    if(instruction.equals("LEFTARROW"))
                            return strInstrToByte("LEFT");
                    if(instruction.equals("RIGHTARROW"))
                            return strInstrToByte("RIGHT");
                    if(instruction.equals("MENU"))
                            return strInstrToByte("APP");
                    if(instruction.equals("WINDOWS"))
                            return strInstrToByte("GUI");
                    if(instruction.equals("PLAY") || instruction.equals("PAUSE"))
                            return strInstrToByte("MEDIA_PLAY_PAUSE");
                    if(instruction.equals("STOP"))
                            return strInstrToByte("MEDIA_STOP");
                    if(instruction.equals("MUTE"))
                            return strInstrToByte("MEDIA_MUTE");
                    if(instruction.equals("VOLUMEUP"))
                            return strInstrToByte("MEDIA_VOLUME_INC");
                    if(instruction.equals("VOLUMEDOWN"))
                            return strInstrToByte("MEDIA_VOLUME_DEC");
                    if(instruction.equals("SCROLLLOCK"))
                            return strInstrToByte("SCROLL_LOCK");
                    if(instruction.equals("NUMLOCK"))
                            return strInstrToByte("NUM_LOCK");
                    if(instruction.equals("CAPSLOCK"))
                            return strInstrToByte("CAPS_LOCK");
                    /* else take first letter */
                    return charToBytes(instruction.charAt(0))[0];
            }
    }
