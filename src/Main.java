import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    /* HTTP Client allows connection to API to be made */
    private static final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();

    /*These variables are needed for the general functioning of the bot such as storing the server name, etc...*/
    public static String serverMessage;
    public static String ipAddress;
    public static String channel;
    public static String topic;
    private static PrintWriter out;
    private static Scanner in;

    /*Needed for help method to be called inside the introduce yourself method.*/
    public static boolean helpCall = false;

    /*Needed in order to generate random numbers for hello function and kick function.*/
    static Random random = new Random();

    /*NamesLists Strings store the names of the people in the channel. */
    static String namesList = "";

    /* These are the variables related to the functioning of the
    copycat function.
     */
    static boolean copyCatMode = false;
    static String copyAggressor;
    static String nameOfCopyVictim;
    static int counter = 1;

    /* These are the variables related to the functioning of the
     * hangman game. */
    static String generatedWord;
    static String guessedCharacters = "";
    static int guessNumber;
    static ArrayList<Character> underscoredWord = new ArrayList<>();
    static int lives;
    static boolean hangmanRunning = false;

    /* These are the variables related to the functioning of the
     * Kanye quote function. */
    static boolean acceptKanyeWarning = false;
    static boolean displayedKanyeWarning = false;

    /* These are the variables related to the functioning of the
     * MemeMaker function. */
    static boolean choosingMemeTemplate = false;
    static int memeTemplate = 0;
    static String bottomText = "";
    static String topText = "";
    static boolean enterBottomText = false;
    static boolean enterTopText = false;
    static boolean sendMeme = false;

    /* Main Method*/
    public static void main(String[] args) throws IOException {
        Scanner console = new Scanner(System.in);

        /* Bot initializer is asked for the Server-Address/IP Address they would like to join */
        System.out.println("What IP Address or web address would you like to join?");
        ipAddress = console.nextLine();

        /* Bot initializer is asked for the port they would like to join the server on*/
        System.out.println("What Port would you like that?");
        String port = console.nextLine();

        /* Bot initializer is asked for the channel they would like to join.*/
        System.out.println("What channel would you like to join? Or enter the name of the new channel you would like to make (without the hash symbol)!");
        channel = console.nextLine();

        /* Bot initializer is asked if they would like to set a topic on join.*/
        System.out.println("Would you like to set a Topic when you join? (type Yes or No)");
        boolean topicDecider = false;
        String topicDecision = console.nextLine();
        if (topicDecision.toLowerCase().equals("yes")) {
            topicDecider = true;
        }

        /* If they do want to set a topic then they can enter the topic subject they want to set.*/
        if (topicDecider) {
            System.out.println("What would you like to set the Topic to?");
            topic = console.nextLine();
        }

        /* Socket object is declared that takes in the Bot Initializers IP Address and Port.*/
        Socket socket = new Socket(ipAddress, Integer.parseInt(port));

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new Scanner(socket.getInputStream());

        //1st Command Setting Nickname
        write("NICK", "MemeBot");
        //2nd Command Setting Username/Creator
        write("USER", "MemeBot 8 * :Azhar's Bot");
        //3rd Command Joining Channel
        write("JOIN", "#" + channel);
        //4th Command setting Topic of Channel
        if (topicDecider) {
            write("TOPIC ", "#" + channel + " :" + topic);
        }

        while (in.hasNext()) {
            serverMessage = in.nextLine();
            System.out.println("<<< " + serverMessage);

            //This is the function for the bot to say introduce itself to the whole channel.
            introduceYourself();

            //This is the function for the bot to say hello to any user that wants to say hello to the bot.
            sayHello();

            //These 3 methods are used in the copycat function of the bot.
            copyCat();
            sendCopyCatMessage(serverMessage);
            stopCopyCat();

            //This is the function for the bot to output a 'rickroll'.
            rickroll();

            //This is the function for the bot to output a dice roll.
            rollDice();

            //These 3 methods are used in the hangman function of the bot.
            guessHangman();
            startHangman();
            stopHangman();

            //This is the function for the bot to output a dad joke.
            dadJoke();

            //These 2 methods are used in the Kanye Quote function of the bot.
            kanyeBotAcceptance();
            kanyeQuote();

            //These 5 methods are used to generate a meme in the MemeMaker function of the bot.
            bottomTextChoose();
            topTextChoose();
            choosingMemeTemplate();
            memeMaker();
            getMeme();

            //This is the function for the bot to stop timing out in a server.
            timeoutStopper();

            //This is the function for the bot to kick someone out of a channel.
            kickUser();

            //This is the function for the bot to leave a channel.
            leave();

            //This is the function for the bot to display all its commands.
            help();
        }
        in.close();
        out.close();
        socket.close();

        System.out.println("done");
    }

    /* Method takes in the IRC Command and Message and processes the messages it recieves */
    private static void write(String command, String message) {
        String fullMessage = command + " " + message;
        System.out.println(">>> " + fullMessage);
        out.print(fullMessage + "\r\n");
        out.flush();
    }

    /********************************** LOGIC OF THE FUNCTIONS ********************************************************/

    /* Method gets all the names of the people that are on the current channel
     * and then greets them and informs them what the bots functions are*/
    private static void introduceYourself() {
        int beginningOfNames = 0;
        if (serverMessage.contains("353")) {
            if (serverMessage.contains("@MemeBot")) {
                beginningOfNames = (serverMessage.substring(2).indexOf(":")) + 11;
            } else {
                beginningOfNames = (serverMessage.substring(2).indexOf(":")) + 10;
            }
            namesList = serverMessage.substring(beginningOfNames);
        }

        //I use the namesList variable to store names for its introductory message when the bot loads into the channel.
        if (serverMessage.contains("End of NAMES list")) {
            helpCall = true;
            //5th Command Tells the server channel he has joined
            write("NOTICE ", "#" + channel + " : MemeBot has joined! Please remember to give him Operator Privileges!");
            //6th Command Introduces himself to everyone in the channel
            write("PRIVMSG ", "#" + channel + " : Hello," + namesList + ". I am MemeBot! Please remember to give me Operator Privileges! My Commands are as follows:");
            help();
        }
    }

    /* Method generates a random number and uses that to pick a hello greeting when the bot
     * greets someone says "hello memebot". A call to getSenderName is made to get the name
     * of the person who has greeted the bot. */
    public static void sayHello() {
        if (serverMessage.toLowerCase().contains("memebot hello")) {
            int number = random.nextInt(7);
            if (number == 0) {
                write("PRIVMSG ", "#" + channel + " : Yo " + getSenderName() + " WASSSSSSUUUUUUUUUUPPPPPP");
            } else if (number == 1) {
                write("PRIVMSG ", "#" + channel + " : OOOOOHHHHH look guys its " + getSenderName() + "! I told you about this guy remember?");
            } else if (number == 2) {
                write("PRIVMSG ", "#" + channel + " : oh... hey " + getSenderName() + "...");
            } else if (number == 3) {
                write("PRIVMSG ", "#" + channel + " : Greetings and salutations, " + getSenderName());
            } else if (number == 4) {
                write("PRIVMSG ", "#" + channel + " : Hello " + getSenderName());
            } else if (number == 5) {
                write("PRIVMSG ", "#" + channel + " : I've been waiting for you " + getSenderName() + ", we meet again at last. The circle is now complete... uh I forget the rest.");
            } else if (number == 6) {
                write("PRIVMSG ", "#" + channel + " : Hey " + getSenderName() + ", Its been a while!");
            }
        }
    }

    /* Method generates activates copycat mode and stores the person who has initiated
     * the copycat mode and the person who the copycat target is. */
    public static void copyCat() {
        if (serverMessage.toLowerCase().contains("memebot copycat")) {
            String[] splitMessage = serverMessage.split(":");
            if (splitMessage[2].length() > 16) {
                copyCatMode = true;
                int indexOfBeginningOfCopiedName = serverMessage.substring(1).indexOf(":") + 18;
                copyAggressor = getSenderName();
                nameOfCopyVictim = serverMessage.substring(indexOfBeginningOfCopiedName);
                System.out.println(nameOfCopyVictim);
                write("PRIVMSG ", "#" + channel + " :  Copycat Mode Activated on " + nameOfCopyVictim + "! " + getSenderName() + " has to type \u000307\"MemeBot STOP\"\u0003 to stop me copying the victim! ");
            } else {
                write("PRIVMSG ", "#" + channel + " :  No Victim specified! Try again.");
            }
        }
    }

    /* Method gets the message that is sent by the victim and copies it to a string and then
     * sends the message back into the chat with increasingly mimicking tones.
     * Just for some extra fun, if the person tries to outsmart memebot by saying things
     * such as memebot is stupid, I'm or im, their name will be replaced there instead */
    public static void sendCopyCatMessage(String serverMessage) {
        if (!serverMessage.substring(0, 4).contains("PING")) {
            if (copyCatMode) {
                if (getSenderName().toLowerCase().equals(nameOfCopyVictim.toLowerCase())) {
                    int indexOfBeginningOfCopiedMessage = serverMessage.substring(1).indexOf(":") + 2;
                    String copiedMessage = serverMessage.toLowerCase().substring(indexOfBeginningOfCopiedMessage);
                    if (copiedMessage.toLowerCase().contains("im") || copiedMessage.contains("i'm") || copiedMessage.contains("memebot")) {
                        copiedMessage = copiedMessage.replace("im", nameOfCopyVictim);
                        copiedMessage = copiedMessage.replace("i'm", nameOfCopyVictim);
                        copiedMessage = copiedMessage.replace("memebot", nameOfCopyVictim);
                    }
                    if (counter == 1) {
                        write("PRIVMSG ", "#" + channel + " :  *says in mimicking tone* " + copiedMessage);
                        counter++;

                    } else if (counter == 2) {
                        write("PRIVMSG ", "#" + channel + " :  *says in increasing mimicking tone* " + copiedMessage);
                        counter++;

                    } else if (counter == 3) {
                        write("PRIVMSG ", "#" + channel + " :  *says in the most mimicking tone* " + copiedMessage);

                    }
                }
            }
        }
    }

    /* Method stops copycat mode and resets the mimicking counter to 0 */
    public static void stopCopyCat() {
        if (serverMessage.toLowerCase().contains("memebot stop") && getSenderName().equals(copyAggressor) && copyCatMode) {
            write("PRIVMSG ", "#" + channel + " : Fine, I'll stop, you killjoy :(");
            copyCatMode = false;
            counter = 1;
        }
    }

    /* rickroll generates an ascii of rick astley using privmsg, it also colours the text and informs
     * the user that if they want to see the ascii properly they should change their font */
    public static void rickroll() {
        if (serverMessage.toLowerCase().contains("memebot rickroll")) {
            write("PRIVMSG ", "#" + channel + " : …………………………………………. ………………………………….,-~~”””’~~–,,_");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. …………………………….,-~”-,:::::::::::::::::::”-,");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………………………..,~”::::::::’,::::::: :::::::::::::|’,");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………………………..|::::::,-~”’___””~~–~”’:}");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………………………..’|:::::|: : : : : : : : : : : : : :");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………………………..|:::::|: : :-~~—: : : —–: |");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ……………………….(_”~-’: : : : : : : : :");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………………………..”’~-,|: : : : : : ~—’: : : :,’–never gonna");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ……………………………|,: : : : : :-~~–: : ::/ —–give you up!");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ……………………….,-”\\’:\\: :’~,,_: : : : : _,-’");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………………….__,-’;;;;;\\:”-,: : : :’~—~”/|");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. ………….__,-~”;;;;;;/;;;;;;;\\: :\\: : :____/: :’,__");
            write("PRIVMSG ", "#" + channel + " : ………………………………………….. .,-~~~””_;;;;;;;;;;;;;;;;;;;;;;;;;’,. .”-,:|:::::::|. . |;;;;”-,__");
            write("PRIVMSG ", "#" + channel + " : …………………………………………../;;;;;;;;;;;;;;;;;;;;;;;;;;;;,;;;;;;;;;\\. . .”|::::::::|. .,’;;;;;;;;;;”-,");
            write("PRIVMSG ", "#" + channel + " : …………………………………………,’ ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;|;;;;;;;;;;;\\. . .\\:::::,’. ./|;;;;;;;;;;;;;|");
            write("PRIVMSG ", "#" + channel + " : ………………………………………,-”;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\\;;;;;;;;;;;’,: : __|. . .|;;;;;;;;;,’;;|");
            write("PRIVMSG ", "#" + channel + " : …………………………………….,-”;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;’,;;;;;;; ;;;; \\. . |:::|. . .”,;;;;;;;;|;;/");
            write("PRIVMSG ", "#" + channel + " : ……………………………………/;;;;;;;;;;;;;;;;;;;;;;;;;;|;;;;;;;;;;;;;;\\;;;;;;;; ;;;\\. .|:::|. . . |;;;;;;;;|/");
            write("PRIVMSG ", "#" + channel + " : …………………………………./;;,-’;;;;;;;;;;;;;;;;;;;;;;,’;;;;;;;;;;;;;;;;;,;;;;;;; ;;;|. .\\:/. . . .|;;;;;;;;|");
            write("PRIVMSG ", "#" + channel + " : …………………………………/;;;;;;;;;;;;;;;;;;;;;;;;;;,;;;;;;;;;;;;;;;;;;;;;;; ;;;;;;;”,: |;|. . . . \\;;;;;;;|");
            write("PRIVMSG ", "#" + channel + " : ………………………………,~”;;;;;;;;;; ;;;;;;;;;;;,-”;;;;;;;;;;;;;;;;;;;;;;;;;;\\;;;;;;;;|.|;|. . . . .|;;;;;;;|");
            write("PRIVMSG ", "#" + channel + " : …………………………..,~”;;;;;;;;;;;;;; ;;;;;;;;,-’;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;’,;;;;;;| |:|. . . . |\\;;;;;;;|");
            write("PRIVMSG ", "#" + channel + " : ………………………….,’;;;;;;;;;;;;;;;;; ;;;;;;;/;;;,-’;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;,;;;;;| |:|. . . .’|;;’,;;;;;|");
            write("PRIVMSG ", "#" + channel + " : …………………………|;,-’;;;;;;;;;;;;;;;;;;;,-’;;;,-’;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;,;;;;| |:|. . .,’;;;;;’,;;;;|_");
            write("PRIVMSG ", "#" + channel + " : …………………………/;;;;;;;;;;;;;;;;;,-’_;;;;;;,’;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;|;;; ;|.|:|. . .|;;;;;;;|;;;;|””~-,");
            write("PRIVMSG ", "#" + channel + " : ………………………./;;;;;;;;;;;;;;;;;;/_”,;;;,’;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ,;;| |:|. . ./;;;;;;;;|;;;|;;;;;;|-,,__");
            write("PRIVMSG ", "#" + channel + " : ……………………../;;;;;;;;;;;;;;;;;,-’…|;;,;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ;;;;;| |:|._,-’;;;;;;;;;|;;;;|;;;;;;;;;;;”’-,_");
            write("PRIVMSG ", "#" + channel + " : ……………………/;;;;;;;;;;;;;;;;,-’….,’;;,;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ;;;;;;;;|.|:|::::”’~–~”’||;;;;;|;;;;;;;;;;,-~””~–,");
            write("PRIVMSG ", "#" + channel + " : ………………….,’;;;;;;;;;;;;;;;;,’……/;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ;;|.|:|::::::::::::::|;;;;;’,;;;;;;;;;”-,: : : : : :”’~-,:”’~~–,");
            write("PRIVMSG ", "#" + channel + " : …………………/;;;;;;;;;;;;;;;,-’……,’;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ;;;;;;;;;;;;|:|:|::::::::::::::’,;;;;;;|_””~–,,-~—,,___,-~~”’__”~-");
            write("PRIVMSG ", "#" + channel + " : ………………,-’;;;;;;;;;;;;;;;,’……../;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ;;;;|:|:|:::::::::::::::|;;;;;;|……………… …”-,\\_”-,”-,”~");
            write("PRIVMSG ", "#" + channel + " : Never gonna \u000305give\u0003 you up...");
            write("PRIVMSG ", "#" + channel + " : Never gonna \u000306let\u0003 you down...");
            write("PRIVMSG ", "#" + channel + " : Never gonna run \u000307around\u0003 and desert you...");
            write("PRIVMSG ", "#" + channel + " : If the ASCII didn't appear correctly, please switch your IRC Client's Font to Nirmala UI or Lucida Grande");
        }
    }

    /*Roll dice generates a random number between 0-5 and then adds one to make up
     * for the offset that is made when random generator methods are used as the bounds
     * of a random generated number are from 0-5. A PRIVMSG is then sent containing the rolled number*/
    public static void rollDice() {
        if (serverMessage.toLowerCase().contains("memebot rolldice")) {
            int number = random.nextInt(6);
            number = number + 1;
            write("PRIVMSG ", "#" + channel + " : The Number you have rolled is " + number + "!");
        }
    }

    /* Method is used for when the IRC User guesses in the hangman game. If they guess more than one character
     * then the Bot interprets that as them trying to guess the word and therefore tells them if they got the
     * word or not. Otherwise it interprets them as trying to guess one character and there are consequences
     * such as taking away lives because of this.*/
    public static void guessHangman() {
        if (!serverMessage.substring(0, 4).contains("PING")) {
            if (hangmanRunning) {
                String[] guess = serverMessage.split(":");
                if (guess[2].length() > 1) {
                    if (guess[2].equals(generatedWord.toLowerCase())) {
                        guessNumber++;
                        write("PRIVMSG ", "#" + channel + " : Congratulations, you guessed the word! You found the word in " + guessNumber + " guesses!");
                        hangmanRunning = false;
                    } else {
                        guessNumber++;
                        write("PRIVMSG ", "#" + channel + " : That's not the word, keep guessing!");
                    }
                } else if (guess[2].length() == 1) {
                    char guessedLetter = guess[2].toLowerCase().charAt(0);
                    if (guessedCharacters.contains(new String(String.valueOf(guessedLetter)))) {
                        write("PRIVMSG ", "#" + channel + " : You've already guessed that letter!");
                    } else {
                        ArrayList<Integer> positions = new ArrayList<>();
                        guessedCharacters += guessedLetter;
                        guessNumber++;
                        for (int i = 0; i < generatedWord.length(); i++) {
                            if (generatedWord.toLowerCase().charAt(i) == (guessedLetter)) {
                                positions.add(i);
                            }
                        }
                        if (!positions.isEmpty()) {
                            for (int position : positions) {
                                underscoredWord.set(position, guessedLetter);
                            }
                            write("PRIVMSG ", "#" + channel + " : You found a letter! " + underscoredWord.toString().toLowerCase().replace("[", "").replace("]", "").replace(",", ""));
                            positions.clear();
                        } else {
                            lives--;
                            write("PRIVMSG ", "#" + channel + " : That letter isn't in the word! You've lost a life!");
                            if (lives != 0) {
                                write("PRIVMSG ", "#" + channel + " : Your life count is now " + lives + ". Guess Again!");
                            }
                        }
                    }
                }
            }
        }
    }

    /* Method starts the hangman function and generates a random word from the dictionary function (down below)
     * and also then sets all the values to their original form such as life count to 0. Finally, the rules are
     * explained to the user.*/
    public static void startHangman() {
        if (!hangmanRunning) {
            underscoredWord.clear();
            guessedCharacters = "";
        }
        if (serverMessage.toLowerCase().contains("memebot hangman")) {
            hangmanRunning = true;
            lives = 6;
            guessNumber = 0;
            generatedWord = dictionaryStringPicker();
            for (int i = 0; i < generatedWord.length(); i++) {
                underscoredWord.add('_');
            }
            String hiddenWord = underscoredWord.toString().replace("[", "").replace("]", "").replace(",", "");
            write("PRIVMSG ", "#" + channel + " : Generating a random word!");
            write("PRIVMSG ", "#" + channel + " : Your life count is at 6");
            write("PRIVMSG ", "#" + channel + " : Your word is as follows \"" + hiddenWord + " \"");
            write("PRIVMSG ", "#" + channel + " : If you would like to end the game, type \u000307\"MemeBot STOP\"\u0003");
            write("PRIVMSG ", "#" + channel + " : \u000307Guess the separate letters contained within the word or guess the whole word if you think you can! No need to prefix letters or words with my name!\u0003");
        }
    }


    /* If the user calls the Memebot Stop function then the game gets stopped. If they find the word the game also gets
     * stopped and finally, if their lives reach zero then they are informed of this case and also the game is stopped. */
    public static void stopHangman() {
        if (hangmanRunning) {
            if (serverMessage.toLowerCase().contains("memebot stop")) {
                hangmanRunning = false;
                write("PRIVMSG ", "#" + channel + " : Hangman has been ended :(");
            } else if (generatedWord.toLowerCase().equals(underscoredWord.toString().toLowerCase().replace("[", "").replace("]", "").replace(",", "").replace(" ", ""))) {
                hangmanRunning = false;
                write("PRIVMSG ", "#" + channel + " : Congratulations, you won! You found the word in " + guessNumber + " guesses!");
            } else if (lives == 0) {
                hangmanRunning = false;
                write("PRIVMSG ", "#" + channel + " : Game Over! You've run out of lives, the word was " + generatedWord);
                write("PRIVMSG ", "#" + channel + " : To play again, type \u000307\"MemeBot Hangman\"\u0003");
            }
        }
    }

    /* Logic behind get requests. A API URl is passed in and the body of the plain text is returned */
    private static String getRequest(String URL) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(URL)).setHeader("User-Agent", "MemeBot School Project").setHeader("Accept", "text/plain").build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /* A dad Joke is requested from the given api site and a call to the get request function is made. a joke is returned
     *  and the PRIVMSG command is used to output this joke.*/
    public static void dadJoke() {
        if (serverMessage.toLowerCase().contains("memebot dadjoke")) {
            try {
                String joke = getRequest("https://icanhazdadjoke.com/");
                write("PRIVMSG ", "#" + channel + " : " + joke);
            } catch (Exception e) {
                write("PRIVMSG ", "#" + channel + " : Couldn't get dad joke right now! Please try again later.");
            }
        }
    }

    /* A warning is generated if the IRC User wants to tolerate the Kanye Quote functions harsh language */
    public static void kanyeBotAcceptance() {
        if (!acceptKanyeWarning && displayedKanyeWarning) {
            write("PRIVMSG ", "#" + channel + " : \u000302Kanye's quotes can have offensive language, be inappropriate and have an EXTREME amount of profanity. Are you sure you want to run this command. Type \"MemeBot Yes\" or \"MemeBot No\" \u0003");
            if (serverMessage.toLowerCase().contains("memebot yes")) {
                acceptKanyeWarning = true;
                write("PRIVMSG ", "#" + channel + " : Activated! YOU HAVE BEEN WARNED! Rewrite \u000304\"MemeBot KanyeQuote\"\u0003 for a quote!");
            } else if (serverMessage.toLowerCase().contains("memebot no")) {
                write("PRIVMSG ", "#" + channel + " : Kanye Quotes has not been activated.");
                displayedKanyeWarning = false;
            }
        }
    }

    /* if the user accepts the Kanye Quote warnings then a get request is made to the api of kanye and the quote is returned
     * as a string and output to the channel.*/
    public static void kanyeQuote() {
        if (serverMessage.toLowerCase().contains("memebot kanyequote")) {
            if (!acceptKanyeWarning && !displayedKanyeWarning) {
                displayedKanyeWarning = true;
                kanyeBotAcceptance();
            } else {
                if (acceptKanyeWarning) {
                    try {
                        String quote = getRequest("https://api.kanye.rest?format=text");
                        write("PRIVMSG ", "#" + channel + " : " + quote);
                    } catch (Exception e) {
                        write("PRIVMSG ", "#" + channel + " : Error, Kanye isn't available right now. Try again later.");
                    }
                }
            }
        }
    }

    /* In order to form a post request this method takes in the parameters and uses string builder
     * to form a valid post request parameter that can be interpreted by API's*/
    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> inputs) {
        var builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : inputs.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    /* The paramters for the meme are inserted into this hashmap alongisde the username of the API website and the
     * password. The URL is passed in and the above method is called to assemble a valid post request. Finally the
     * memeurl is returned. */
    private static String doPost(String URL) throws Exception {
        Map<Object, Object> inputs = new HashMap<>();
        inputs.put("template_id", memeTemplate);
        inputs.put("username", ""); //REMOVED
        inputs.put("password", ""); //REMOVED
        inputs.put("text0", topText);
        inputs.put("text1", bottomText);
        HttpRequest request = HttpRequest.newBuilder().POST(ofFormData(inputs)).uri(URI.create(URL)).setHeader("User-Agent", "MemeBot School Project").header("Content-Type", "application/x-www-form-urlencoded").build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /* IRC User inserts the bottom text they want for their chosen meme and the server message is cut down to
     * get the content of the bottom text they want to use in their meme using array splitting.*/
    public static void bottomTextChoose() {
        if (!serverMessage.substring(0, 4).contains("PING")) {
            if (enterBottomText) {
                String[] serverMessageSplit = serverMessage.split(":");
                bottomText = serverMessageSplit[2];
                write("PRIVMSG ", "#" + channel + " : Meme is being generated...");
                enterBottomText = false;
                sendMeme = true;
            }
        }
    }

    /* IRC User inserts the top text they want for their chosen meme and the server message is cut down to
     * get the content of the top text they want to use in their meme using array splitting.*/
    public static void topTextChoose() {
        if (!serverMessage.substring(0, 4).contains("PING")) {
            if (enterTopText) {
                String[] serverMessageSplit = serverMessage.split(":");
                topText = serverMessageSplit[2];
                write("PRIVMSG ", "#" + channel + " : Enter bottom text. If you would like to leave blank, enter \"-\".");
                enterTopText = false;
                enterBottomText = true;
            }
        }
    }

    /* The program matches the meme template name entered by the IRC User to a range of codes that, in the API,
     * match their respective memes.*/
    public static void choosingMemeTemplate() {
        if (!serverMessage.substring(0, 4).contains("PING")) {
            if (choosingMemeTemplate) {
                if (serverMessage.toLowerCase().contains("bad luck brian")) {
                    memeTemplate = 61585;
                } else if (serverMessage.toLowerCase().contains("batman slapping robin")) {
                    memeTemplate = 438680;
                } else if (serverMessage.toLowerCase().contains("surprised pikachu")) {
                    memeTemplate = 155067746;
                } else if (serverMessage.toLowerCase().contains("first world problems")) {
                    memeTemplate = 61539;
                } else if (serverMessage.toLowerCase().contains("patrick")) {
                    memeTemplate = 61581;
                }
                write("PRIVMSG ", "#" + channel + " : Enter top text. If you would like to leave blank, enter \"-\".");
                choosingMemeTemplate = false;
                enterTopText = true;
            }
        }
    }

    /* Method gives instructions to the IRC User about choosing a meme template and how they should utilize,
     * the function.*/
    public static void memeMaker() {
        if (serverMessage.toLowerCase().contains("memebot mememaker")) {
            write("PRIVMSG ", "#" + channel + " : What meme template would you like to use? We have the following available: Bad Luck Brian, Batman slapping Robin, Surprised Pikachu, First World Problems, Patrick.");
            write("PRIVMSG ", "#" + channel + " : While using MemeMaker, you do not need to preface the entry information with \"MemeBot\"");
            choosingMemeTemplate = true;
        }
    }

    /* After the IRC User has entered their preferences, the API Post is made and a URL is returned and put into
     * a PRIVMSG. If some of the paramters they have specified are invalid then the method returns a form of error
     * message. */
    public static void getMeme() {
        if (sendMeme) {
            try {
                String memeURL = doPost("https://api.imgflip.com/caption_image");
                int beginningOfMeme = memeURL.indexOf("https");
                int endOfMeme = memeURL.indexOf("\",");
                memeURL = memeURL.substring(beginningOfMeme, endOfMeme);
                memeURL = memeURL.replace("\\", "");
                write("PRIVMSG ", "#" + channel + " :" + memeURL);
                sendMeme = false;
                memeTemplate = 0;
            } catch (Exception e) {
                write("PRIVMSG ", "#" + channel + " : Can't build meme, check what you've entered!");
                sendMeme = false;
                memeTemplate = 0;
            }
        }
    }

    /* PONG function is utilized in order to stop the bot from timing out*/
    public static void timeoutStopper() {
        if (serverMessage.startsWith("PING")) {
            String pingContents = ipAddress;
            //7th Command, Sends back PONG when PING is received
            write("PONG", pingContents);
        }
    }

    /* The name of the IRC User that is going to be kicked is stored in a stirng using array splitting. The method
     * then generates a random funny message that is sent just before the IRC User is kicked. The IRC User is then
     * kicked out of the channel using the KICK command.*/
    public static void kickUser() {
        if (serverMessage.toLowerCase().contains("memebot kick ")) {
            String[] kickSplit = serverMessage.toLowerCase().split(":");
            String nameOfVictim = kickSplit[2].replace("memebot kick", "");
            int number = random.nextInt(7);
            if (number == 0) {
                write("PRIVMSG ", "#" + channel + " : MemeBot grabs" + nameOfVictim + " by the scruff of his neck. Opens door and...");
            } else if (number == 1) {
                write("PRIVMSG ", "#" + channel + " : MemeBot moonwalks towards" + nameOfVictim + " like Michael Jackson. \u000309hehe\u0003, kicks" + nameOfVictim + " out the door.");
            } else if (number == 2) {
                write("PRIVMSG ", "#" + channel + " : Hey " + nameOfVictim + ", can I show you something? Look its outside... *Quickly runs back inside the house and shut door*");
            } else if (number == 3) {
                write("PRIVMSG ", "#" + channel + " : https://www.youtube.com/watch?v=qvJeATp31dw");
            } else if (number == 4) {
                write("PRIVMSG ", "#" + channel + " : Bye Bye" + nameOfVictim);
            } else if (number == 5) {
                write("PRIVMSG ", "#" + channel + " : https://www.youtube.com/watch?v=gG5es-0es0o");
            } else if (number == 6) {
                write("PRIVMSG ", "#" + channel + " : Sorry, " + nameOfVictim);
            }
            //8th Command, Kicks a specified user out of the channel
            write("KICK", "#" + channel + " " + nameOfVictim + " :Now, you go think about what you have done!");
        }

    }

    /* When an IRC User says MemeBot leave, MemeBot will display a humurous message and then leave the IRC Channel.*/
    public static void leave() {
        if (serverMessage.toLowerCase().contains("memebot leave")) {
            write("PRIVMSG ", "#" + channel + " : Fine i'll go :( *slowly walks towards door with sad violin playing in the background*");
            //9th Command, Leaves the Channel
            write("PART", "#" + channel + " :Bye");
        }
    }

    /* When IRC User enters MemeBot help, all the functions of my IRC bot are displayed. A call to this method is also
     * made in the introduction method, this is for efficiency and so I don't waste space.*/
    public static void help() {
        if (serverMessage.toLowerCase().contains("memebot help") || helpCall) {
            write("PRIVMSG ", "#" + channel + " : \u000307Hello\u0003 - I will say hello with a customised response to anyone");
            write("PRIVMSG ", "#" + channel + " : \u000307Copycat <insert name here>\u0003 - I will copy and mimic the victim whose name is inserted until my stop command is called");
            write("PRIVMSG ", "#" + channel + " : \u000307RickRoll\u0003 - I will RickRoll the channel");
            write("PRIVMSG ", "#" + channel + " : \u000307RollDice\u0003 - I will roll a dice for the channel");
            write("PRIVMSG ", "#" + channel + " : \u000307Hangman\u0003 - I will start a game of hangman");
            write("PRIVMSG ", "#" + channel + " : \u000307DadJoke\u0003 - I will make a dad joke");
            write("PRIVMSG ", "#" + channel + " : \u000307KanyeQuote\u0003 - I will generate a Kanye West Quote");
            write("PRIVMSG ", "#" + channel + " : \u000307MemeMaker\u0003 - You will be able to make a custom meme");
            write("PRIVMSG ", "#" + channel + " : \u000307Kick <insert name here>\u0003 - You will be able to kick a user from the channel with a funny message");
            write("PRIVMSG ", "#" + channel + " : \u000307Leave\u0003 - I will leave the channel");
            write("PRIVMSG ", "#" + channel + " : \u000307Help\u0003 - I will show all my commands to you");
            write("PRIVMSG ", "#" + channel + " : Please remember, in order to use my commands you have to call my name (MemeBot) before each command unless I say you don't have to!");
            helpCall = false;
        }
    }

    /* Get sender name is a short cut method in order to get the name of who sent a message. This is done
     * by String substrings. This method is specifically utilized in the copycat function. */
    public static String getSenderName() {
        int nameBeginningIndex = 1;
        int nameEndIndex = serverMessage.substring(0, 27).indexOf("!");
        return serverMessage.substring(nameBeginningIndex, nameEndIndex);
    }

    /* A random number is generated and the dictionary reads the Dictionary txt using that random number and lands
     * on a random line that has a random word. This method is utilized in the hangman game where a random word needs
     * to be generated.*/
    public static String dictionaryStringPicker() {
        int number = random.nextInt(1525);
        String line = null;
        try {
            FileReader fr = new FileReader("./Dictionary.txt");
            BufferedReader br = new BufferedReader(fr);
            for (int i = 0; i < number; i++) {
                line = br.readLine();
            }
            br.close();
            fr.close();
            return line;
        } catch (Exception e) {
            e.printStackTrace();
        }
        write("PRIVMSG ", "#" + channel + " : Word couldn't be generated, Try Again Later");
        return null;
    }
}
