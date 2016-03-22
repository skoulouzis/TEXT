/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.champeau.ld.UberLanguageDetector;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

/**
 *
 * @author S. Koulouzis
 */
class Utils {

    public static final String propertiesPath = "text.properties";
    static Set<String> stopwords = new HashSet();
    private static UberLanguageDetector uberLanguageDetector;

    private static void populateStopWords() {
        stopwords.add("a");
        stopwords.add("aaa");
        stopwords.add("able");
        stopwords.add("about");
        stopwords.add("above");
        stopwords.add("abroad");
        stopwords.add("abst");
        stopwords.add("accordance");
        stopwords.add("according");
        stopwords.add("accordingly");
        stopwords.add("across");
        stopwords.add("act");
        stopwords.add("actually");
        stopwords.add("added");
        stopwords.add("adj");
        stopwords.add("adopted");
        stopwords.add("affected");
        stopwords.add("affecting");
        stopwords.add("affects");
        stopwords.add("after");
        stopwords.add("afterwards");
        stopwords.add("again");
        stopwords.add("against");
        stopwords.add("ago");
        stopwords.add("ah");
        stopwords.add("ahead");
        stopwords.add("ain\'t");
        stopwords.add("all");
        stopwords.add("allow");
        stopwords.add("allows");
        stopwords.add("almost");
        stopwords.add("alone");
        stopwords.add("along");
        stopwords.add("alongside");
        stopwords.add("already");
        stopwords.add("also");
        stopwords.add("although");
        stopwords.add("always");
        stopwords.add("am");
        stopwords.add("amid");
        stopwords.add("amidst");
        stopwords.add("among");
        stopwords.add("amongst");
        stopwords.add("amoungst");
        stopwords.add("amount");
        stopwords.add("an");
        stopwords.add("and");
        stopwords.add("announce");
        stopwords.add("another");
        stopwords.add("any");
        stopwords.add("anybody");
        stopwords.add("anyhow");
        stopwords.add("anymore");
        stopwords.add("anyone");
        stopwords.add("anything");
        stopwords.add("anyway");
        stopwords.add("anyways");
        stopwords.add("anywhere");
        stopwords.add("apart");
        stopwords.add("apparently");
        stopwords.add("appear");
        stopwords.add("appreciate");
        stopwords.add("appropriate");
        stopwords.add("approximately");
        stopwords.add("are");
        stopwords.add("aren");
        stopwords.add("aren\'t");
        stopwords.add("arent");
        stopwords.add("arise");
        stopwords.add("around");
        stopwords.add("a\'s");
        stopwords.add("as");
        stopwords.add("aside");
        stopwords.add("ask");
        stopwords.add("asking");
        stopwords.add("associated");
        stopwords.add("at");
        stopwords.add("auth");
        stopwords.add("available");
        stopwords.add("away");
        stopwords.add("awfully");
        stopwords.add("b");
        stopwords.add("back");
        stopwords.add("backward");
        stopwords.add("backwards");
        stopwords.add("be");
        stopwords.add("became");
        stopwords.add("because");
        stopwords.add("become");
        stopwords.add("becomes");
        stopwords.add("becoming");
        stopwords.add("been");
        stopwords.add("before");
        stopwords.add("beforehand");
        stopwords.add("begin");
        stopwords.add("beginning");
        stopwords.add("beginnings");
        stopwords.add("begins");
        stopwords.add("behind");
        stopwords.add("being");
        stopwords.add("believe");
        stopwords.add("below");
        stopwords.add("beside");
        stopwords.add("besides");
        stopwords.add("best");
        stopwords.add("better");
        stopwords.add("between");
        stopwords.add("beyond");
        stopwords.add("bill");
        stopwords.add("biol");
        stopwords.add("both");
        stopwords.add("bottom");
        stopwords.add("brief");
        stopwords.add("briefly");
        stopwords.add("but");
        stopwords.add("by");
        stopwords.add("c");
        stopwords.add("ca");
//        stopwords.add("call");
        stopwords.add("came");
        stopwords.add("can");
        stopwords.add("cannot");
        stopwords.add("can\'t");
        stopwords.add("cant");
        stopwords.add("caption");
        stopwords.add("cause");
        stopwords.add("causes");
        stopwords.add("certain");
        stopwords.add("certainly");
        stopwords.add("changes");
        stopwords.add("clearly");
        stopwords.add("c\'mon");
        stopwords.add("co.");
        stopwords.add("co");
        stopwords.add("com");
        stopwords.add("come");
        stopwords.add("comes");
        stopwords.add("con");
        stopwords.add("concerning");
        stopwords.add("consequently");
        stopwords.add("consider");
        stopwords.add("considering");
        stopwords.add("contain");
        stopwords.add("containing");
        stopwords.add("contains");
        stopwords.add("corresponding");
        stopwords.add("could");
        stopwords.add("couldn\'t");
        stopwords.add("couldnt");
        stopwords.add("course");
        stopwords.add("cry");
        stopwords.add("c\'s");
        stopwords.add("currently");
        stopwords.add("d");
        stopwords.add("dare");
        stopwords.add("daren\'t");
        stopwords.add("date");
        stopwords.add("de");
        stopwords.add("definitely");
        stopwords.add("describe");
        stopwords.add("described");
        stopwords.add("despite");
        stopwords.add("detail");
        stopwords.add("did");
        stopwords.add("didn\'t");
        stopwords.add("different");
        stopwords.add("directly");
        stopwords.add("do");
        stopwords.add("does");
        stopwords.add("doesn\'t");
        stopwords.add("doing");
        stopwords.add("done");
        stopwords.add("don\'t");
        stopwords.add("down");
        stopwords.add("downwards");
        stopwords.add("due");
        stopwords.add("during");
        stopwords.add("e");
//        stopwords.add("ee");
        stopwords.add("each");
        stopwords.add("ed");
        stopwords.add("edu");
        stopwords.add("effect");
        stopwords.add("eg");
        stopwords.add("eight");
        stopwords.add("eighty");
        stopwords.add("either");
        stopwords.add("eleven");
        stopwords.add("else");
        stopwords.add("elsewhere");
        stopwords.add("empty");
        stopwords.add("end");
        stopwords.add("ending");
        stopwords.add("enough");
        stopwords.add("entirely");
        stopwords.add("especially");
        stopwords.add("et");
        stopwords.add("et-al");
        stopwords.add("etc");
        stopwords.add("even");
        stopwords.add("ever");
        stopwords.add("evermore");
        stopwords.add("every");
        stopwords.add("everybody");
        stopwords.add("everyone");
        stopwords.add("everything");
        stopwords.add("everywhere");
        stopwords.add("ex");
        stopwords.add("exactly");
        stopwords.add("example");
        stopwords.add("except");
        stopwords.add("f");
        stopwords.add("fairly");
        stopwords.add("far");
        stopwords.add("farther");
        stopwords.add("few");
        stopwords.add("fewer");
        stopwords.add("ff");
        stopwords.add("fifteen");
        stopwords.add("fifth");
        stopwords.add("fify");
        stopwords.add("fill");
        stopwords.add("find");
        stopwords.add("fire");
        stopwords.add("first");
        stopwords.add("five");
        stopwords.add("fix");
        stopwords.add("followed");
        stopwords.add("following");
        stopwords.add("follows");
        stopwords.add("for");
        stopwords.add("forever");
        stopwords.add("former");
        stopwords.add("formerly");
        stopwords.add("forth");
        stopwords.add("forty");
//        stopwords.add("forward");
        stopwords.add("found");
        stopwords.add("four");
        stopwords.add("from");
        stopwords.add("front");
        stopwords.add("full");
        stopwords.add("further");
        stopwords.add("furthermore");
        stopwords.add("g");
        stopwords.add("gave");
        stopwords.add("get");
        stopwords.add("gets");
        stopwords.add("getting");
        stopwords.add("give");
        stopwords.add("given");
        stopwords.add("gives");
        stopwords.add("giving");
        stopwords.add("go");
        stopwords.add("goes");
        stopwords.add("going");
        stopwords.add("gone");
        stopwords.add("got");
        stopwords.add("gotten");
        stopwords.add("greetings");
//        stopwords.add("h");
        stopwords.add("had");
        stopwords.add("hadn\'t");
        stopwords.add("half");
        stopwords.add("happens");
        stopwords.add("hardly");
        stopwords.add("has");
        stopwords.add("hasn\'t");
        stopwords.add("hasnt");
        stopwords.add("have");
        stopwords.add("haven\'t");
        stopwords.add("having");
        stopwords.add("he");
        stopwords.add("he\'d");
        stopwords.add("hed");
        stopwords.add("he\'ll");
        stopwords.add("hello");
        stopwords.add("help");
        stopwords.add("hence");
        stopwords.add("her");
        stopwords.add("here");
        stopwords.add("hereafter");
        stopwords.add("hereby");
        stopwords.add("herein");
        stopwords.add("here\'s");
        stopwords.add("heres");
        stopwords.add("hereupon");
        stopwords.add("hers");
        stopwords.add("herse”");
        stopwords.add("herself");
        stopwords.add("he\'s");
        stopwords.add("hes");
        stopwords.add("hi");
        stopwords.add("hid");
        stopwords.add("him");
        stopwords.add("himse”");
        stopwords.add("himself");
        stopwords.add("his");
        stopwords.add("hither");
        stopwords.add("home");
        stopwords.add("hopefully");
        stopwords.add("how");
        stopwords.add("howbeit");
        stopwords.add("however");
        stopwords.add("how\'s");
        stopwords.add("hundred");
        stopwords.add("i");
        stopwords.add("i\'d");
        stopwords.add("id");
        stopwords.add("ie");
        stopwords.add("if");
        stopwords.add("ignored");
        stopwords.add("i\'ll");
        stopwords.add("i\'m");
        stopwords.add("im");
        stopwords.add("immediate");
        stopwords.add("immediately");
        stopwords.add("importance");
        stopwords.add("important");
        stopwords.add("in");
        stopwords.add("inasmuch");
        stopwords.add("inc.");
        stopwords.add("inc");
        stopwords.add("indeed");
        stopwords.add("index");
        stopwords.add("indicate");
        stopwords.add("indicated");
        stopwords.add("indicates");
        stopwords.add("information");
        stopwords.add("inner");
        stopwords.add("inside");
        stopwords.add("insofar");
        stopwords.add("instead");
        stopwords.add("interest");
        stopwords.add("into");
        stopwords.add("invention");
        stopwords.add("inward");
        stopwords.add("is");
        stopwords.add("isn\'t");
        stopwords.add("it");
        stopwords.add("it\'d");
        stopwords.add("itd");
        stopwords.add("it\'ll");
        stopwords.add("it\'s");
        stopwords.add("its");
        stopwords.add("itse”");
        stopwords.add("itself");
        stopwords.add("i\'ve");
        stopwords.add("j");
        stopwords.add("just");
//        stopwords.add("k");
        stopwords.add("keep");
        stopwords.add("keeps");
        stopwords.add("kept");
        stopwords.add("keys");
        stopwords.add("kg");
        stopwords.add("km");
//        stopwords.add("know");
        stopwords.add("known");
        stopwords.add("knows");
        stopwords.add("l");
        stopwords.add("largely");
        stopwords.add("last");
        stopwords.add("lately");
        stopwords.add("later");
        stopwords.add("latter");
        stopwords.add("latterly");
        stopwords.add("least");
        stopwords.add("less");
        stopwords.add("lest");
        stopwords.add("let");
        stopwords.add("let\'s");
        stopwords.add("lets");
        stopwords.add("like");
        stopwords.add("liked");
        stopwords.add("likely");
        stopwords.add("likewise");
        stopwords.add("line");
        stopwords.add("little");
        stopwords.add("\'ll");
        stopwords.add("look");
        stopwords.add("looking");
        stopwords.add("looks");
        stopwords.add("low");
        stopwords.add("lower");
        stopwords.add("ltd");
        stopwords.add("m");
        stopwords.add("made");
        stopwords.add("mainly");
        stopwords.add("make");
        stopwords.add("makes");
        stopwords.add("many");
        stopwords.add("may");
        stopwords.add("maybe");
        stopwords.add("mayn\'t");
        stopwords.add("me");
        stopwords.add("mean");
        stopwords.add("means");
        stopwords.add("meantime");
        stopwords.add("meanwhile");
        stopwords.add("merely");
        stopwords.add("mg");
        stopwords.add("might");
        stopwords.add("mightn\'t");
        stopwords.add("mill");
        stopwords.add("million");
        stopwords.add("mine");
        stopwords.add("minus");
        stopwords.add("miss");
        stopwords.add("ml");
        stopwords.add("more");
        stopwords.add("moreover");
        stopwords.add("most");
        stopwords.add("mostly");
        stopwords.add("move");
        stopwords.add("mr");
        stopwords.add("mrs");
        stopwords.add("much");
        stopwords.add("mug");
        stopwords.add("must");
        stopwords.add("mustn\'t");
        stopwords.add("my");
        stopwords.add("myse”");
        stopwords.add("myself");
        stopwords.add("n");
        stopwords.add("na");
        stopwords.add("name");
        stopwords.add("namely");
        stopwords.add("nay");
        stopwords.add("nd");
        stopwords.add("near");
        stopwords.add("nearly");
        stopwords.add("necessarily");
        stopwords.add("necessary");
        stopwords.add("need");
        stopwords.add("needn\'t");
        stopwords.add("needs");
        stopwords.add("neither");
        stopwords.add("never");
        stopwords.add("neverf");
        stopwords.add("neverless");
        stopwords.add("nevertheless");
        stopwords.add("new");
        stopwords.add("next");
        stopwords.add("nine");
        stopwords.add("ninety");
        stopwords.add("no");
        stopwords.add("nobody");
        stopwords.add("non");
        stopwords.add("none");
        stopwords.add("nonetheless");
        stopwords.add("no-one");
        stopwords.add("noone");
        stopwords.add("nor");
        stopwords.add("normally");
        stopwords.add("nos");
        stopwords.add("not");
        stopwords.add("noted");
        stopwords.add("nothing");
        stopwords.add("notwithstanding");
        stopwords.add("novel");
        stopwords.add("now");
        stopwords.add("nowhere");
        stopwords.add("o");
        stopwords.add("obtain");
        stopwords.add("obtained");
        stopwords.add("obviously");
        stopwords.add("of");
        stopwords.add("off");
        stopwords.add("often");
        stopwords.add("oh");
        stopwords.add("ok");
        stopwords.add("okay");
        stopwords.add("old");
        stopwords.add("omitted");
        stopwords.add("on");
        stopwords.add("once");
        stopwords.add("one");
        stopwords.add("one\'s");
        stopwords.add("ones");
        stopwords.add("only");
        stopwords.add("onto");
        stopwords.add("opposite");
        stopwords.add("or");
        stopwords.add("ord");
        stopwords.add("other");
        stopwords.add("others");
        stopwords.add("otherwise");
        stopwords.add("ought");
        stopwords.add("oughtn\'t");
        stopwords.add("our");
        stopwords.add("ours ");
        stopwords.add("ours");
        stopwords.add("ourselves");
        stopwords.add("out");
        stopwords.add("outside");
        stopwords.add("over");
        stopwords.add("overall");
        stopwords.add("owing");
        stopwords.add("own");
        stopwords.add("p");
        stopwords.add("page");
        stopwords.add("pages");
        stopwords.add("part");
        stopwords.add("particular");
        stopwords.add("particularly");
        stopwords.add("past");
        stopwords.add("per");
        stopwords.add("perhaps");
        stopwords.add("placed");
        stopwords.add("please");
        stopwords.add("plus");
        stopwords.add("poorly");
        stopwords.add("possible");
        stopwords.add("possibly");
        stopwords.add("potentially");
        stopwords.add("pp");
        stopwords.add("predominantly");
        stopwords.add("present");
        stopwords.add("presumably");
        stopwords.add("previously");
        stopwords.add("primarily");
        stopwords.add("probably");
        stopwords.add("promptly");
        stopwords.add("proud");
        stopwords.add("provided");
        stopwords.add("provides");
        stopwords.add("put");
//        stopwords.add("q");
        stopwords.add("que");
        stopwords.add("quickly");
        stopwords.add("quite");
        stopwords.add("qv");
//        stopwords.add("r");
        stopwords.add("ran");
        stopwords.add("rather");
        stopwords.add("rd");
        stopwords.add("re");
        stopwords.add("readily");
        stopwords.add("really");
        stopwords.add("reasonably");
        stopwords.add("recent");
        stopwords.add("recently");
        stopwords.add("ref");
        stopwords.add("refs");
        stopwords.add("regarding");
        stopwords.add("regardless");
        stopwords.add("regards");
        stopwords.add("related");
        stopwords.add("relatively");
        stopwords.add("research");
        stopwords.add("respectively");
        stopwords.add("resulted");
        stopwords.add("resulting");
//        stopwords.add("results");
        stopwords.add("right");
        stopwords.add("round");
        stopwords.add("run");
//        stopwords.add("s");
        stopwords.add("said");
        stopwords.add("same");
        stopwords.add("saw");
        stopwords.add("say");
        stopwords.add("saying");
        stopwords.add("says");
        stopwords.add("sec");
        stopwords.add("second");
        stopwords.add("secondly");
        stopwords.add("section");
        stopwords.add("see");
        stopwords.add("seeing");
        stopwords.add("seem");
        stopwords.add("seemed");
        stopwords.add("seeming");
        stopwords.add("seems");
        stopwords.add("seen");
        stopwords.add("self");
        stopwords.add("selves");
        stopwords.add("sensible");
        stopwords.add("sent");
        stopwords.add("serious");
        stopwords.add("seriously");
        stopwords.add("seven");
        stopwords.add("several");
        stopwords.add("shall");
        stopwords.add("shan\'t");
        stopwords.add("she");
        stopwords.add("she\'d");
        stopwords.add("shed");
        stopwords.add("she\'ll");
        stopwords.add("she\'s");
        stopwords.add("shes");
        stopwords.add("should");
        stopwords.add("shouldn\'t");
        stopwords.add("show");
        stopwords.add("showed");
        stopwords.add("shown");
        stopwords.add("showns");
        stopwords.add("shows");
        stopwords.add("side");
        stopwords.add("significant");
        stopwords.add("significantly");
        stopwords.add("similar");
        stopwords.add("similarly");
        stopwords.add("since");
        stopwords.add("sincere");
        stopwords.add("six");
        stopwords.add("sixty");
        stopwords.add("slightly");
        stopwords.add("so");
        stopwords.add("some");
        stopwords.add("somebody");
        stopwords.add("someday");
        stopwords.add("somehow");
        stopwords.add("someone");
        stopwords.add("somethan");
        stopwords.add("something");
        stopwords.add("sometime");
        stopwords.add("sometimes");
        stopwords.add("somewhat");
        stopwords.add("somewhere");
        stopwords.add("soon");
        stopwords.add("sorry");
        stopwords.add("specifically");
        stopwords.add("specified");
        stopwords.add("specify");
        stopwords.add("specifying");
        stopwords.add("state");
        stopwords.add("states");
        stopwords.add("still");
        stopwords.add("stop");
        stopwords.add("strongly");
        stopwords.add("sub");
        stopwords.add("substantially");
        stopwords.add("successfully");
        stopwords.add("such");
        stopwords.add("sufficiently");
        stopwords.add("suggest");
        stopwords.add("sup");
        stopwords.add("sure");
//        stopwords.add("system");
        stopwords.add("t");
        stopwords.add("take");
        stopwords.add("taken");
        stopwords.add("taking");
        stopwords.add("tell");
        stopwords.add("ten");
        stopwords.add("tends");
        stopwords.add("th");
        stopwords.add("than");
        stopwords.add("thank");
        stopwords.add("thanks");
        stopwords.add("thanx");
        stopwords.add("that");
        stopwords.add("that\'ll");
        stopwords.add("that\'s");
        stopwords.add("thats");
        stopwords.add("that\'ve");
        stopwords.add("the");
        stopwords.add("their");
        stopwords.add("theirs");
        stopwords.add("them");
        stopwords.add("themselves");
        stopwords.add("then");
        stopwords.add("thence");
        stopwords.add("there");
        stopwords.add("thereafter");
        stopwords.add("thereby");
        stopwords.add("there\'d");
        stopwords.add("thered");
        stopwords.add("therefore");
        stopwords.add("therein");
        stopwords.add("there\'ll");
        stopwords.add("thereof");
        stopwords.add("there\'re");
        stopwords.add("therere");
        stopwords.add("there\'s");
        stopwords.add("theres");
        stopwords.add("thereto");
        stopwords.add("thereupon");
        stopwords.add("there\'ve");
        stopwords.add("these");
        stopwords.add("they");
        stopwords.add("they\'d");
        stopwords.add("theyd");
        stopwords.add("they\'ll");
        stopwords.add("they\'re");
        stopwords.add("theyre");
        stopwords.add("they\'ve");
        stopwords.add("thick");
        stopwords.add("thin");
        stopwords.add("thing");
        stopwords.add("things");
        stopwords.add("think");
        stopwords.add("third");
        stopwords.add("thirty");
        stopwords.add("this");
        stopwords.add("thorough");
        stopwords.add("thoroughly");
        stopwords.add("those");
        stopwords.add("thou");
        stopwords.add("though");
        stopwords.add("thoughh");
        stopwords.add("thousand");
        stopwords.add("three");
        stopwords.add("throug");
        stopwords.add("through");
        stopwords.add("throughout");
        stopwords.add("thru");
        stopwords.add("thus");
        stopwords.add("til");
        stopwords.add("till");
        stopwords.add("tip");
        stopwords.add("to");
        stopwords.add("together");
        stopwords.add("too");
        stopwords.add("took");
        stopwords.add("top");
        stopwords.add("toward");
        stopwords.add("towards");
        stopwords.add("tried");
        stopwords.add("tries");
        stopwords.add("truly");
        stopwords.add("try");
        stopwords.add("trying");
        stopwords.add("t\'s");
        stopwords.add("ts");
        stopwords.add("twelve");
        stopwords.add("twenty");
        stopwords.add("twice");
        stopwords.add("two");
        stopwords.add("u");
        stopwords.add("un");
        stopwords.add("under");
        stopwords.add("underneath");
        stopwords.add("undoing");
        stopwords.add("unfortunately");
        stopwords.add("unless");
        stopwords.add("unlike");
        stopwords.add("unlikely");
        stopwords.add("until");
        stopwords.add("unto");
        stopwords.add("up");
        stopwords.add("upon");
        stopwords.add("ups");
        stopwords.add("upwards");
        stopwords.add("us");
        stopwords.add("use");
        stopwords.add("used");
        stopwords.add("useful");
        stopwords.add("usefully");
        stopwords.add("usefulness");
        stopwords.add("uses");
        stopwords.add("using");
        stopwords.add("usually");
        stopwords.add("uucp");
        stopwords.add("v");
        stopwords.add("value");
        stopwords.add("various");
        stopwords.add("\'ve");
        stopwords.add("versus");
        stopwords.add("very");
        stopwords.add("via");
        stopwords.add("viz");
        stopwords.add("vol");
        stopwords.add("vols");
        stopwords.add("vs");
        stopwords.add("w");
        stopwords.add("want");
        stopwords.add("wants");
        stopwords.add("was");
        stopwords.add("wasn\'t");
        stopwords.add("way");
        stopwords.add("we");
        stopwords.add("we\'d");
        stopwords.add("wed");
        stopwords.add("welcome");
        stopwords.add("we\'ll");
        stopwords.add("well");
        stopwords.add("went");
        stopwords.add("we\'re");
        stopwords.add("were");
        stopwords.add("weren\'t");
        stopwords.add("we\'ve");
        stopwords.add("what");
        stopwords.add("whatever");
        stopwords.add("what\'ll");
        stopwords.add("what\'s");
        stopwords.add("whats");
        stopwords.add("what\'ve");
        stopwords.add("when");
        stopwords.add("whence");
        stopwords.add("whenever");
        stopwords.add("when\'s");
        stopwords.add("where");
        stopwords.add("whereafter");
        stopwords.add("whereas");
        stopwords.add("whereby");
        stopwords.add("wherein");
        stopwords.add("where\'s");
        stopwords.add("wheres");
        stopwords.add("whereupon");
        stopwords.add("wherever");
        stopwords.add("whether");
        stopwords.add("which");
        stopwords.add("whichever");
        stopwords.add("while");
        stopwords.add("whilst");
        stopwords.add("whim");
        stopwords.add("whither");
        stopwords.add("who");
        stopwords.add("who\'d");
        stopwords.add("whod");
        stopwords.add("whoever");
        stopwords.add("whole");
        stopwords.add("who\'ll");
        stopwords.add("whom");
        stopwords.add("whomever");
        stopwords.add("who\'s");
        stopwords.add("whos");
        stopwords.add("whose");
        stopwords.add("why");
        stopwords.add("why\'s");
        stopwords.add("widely");
        stopwords.add("will");
        stopwords.add("willing");
        stopwords.add("wish");
        stopwords.add("with");
        stopwords.add("within");
        stopwords.add("without");
        stopwords.add("wonder");
        stopwords.add("won\'t");
        stopwords.add("words");
        stopwords.add("world");
        stopwords.add("would");
        stopwords.add("wouldn\'t");
        stopwords.add("www");
//        stopwords.add("x");
//        stopwords.add("y");
        stopwords.add("yes");
        stopwords.add("yet");
        stopwords.add("you");
        stopwords.add("you\'d");
        stopwords.add("youd");
        stopwords.add("you\'ll");
        stopwords.add("your");
        stopwords.add("you\'re");
        stopwords.add("youre");
        stopwords.add("yours");
        stopwords.add("yourself");
        stopwords.add("yourselves");
        stopwords.add("you\'ve");
//        stopwords.add("z");
        stopwords.add("zero");
        stopwords.add("zero﻿able");
        stopwords.add("zero﻿I");
        stopwords.add("andor");
        
        
    }

    static boolean isStopWord(String word) {
        if (stopwords.isEmpty()) {
            populateStopWords();
        }
        return stopwords.contains(word);
    }

    static CharArraySet getCharArrayStopwords() {
        if (stopwords.isEmpty()) {
            populateStopWords();
        }
        return new CharArraySet(Version.LUCENE_42, stopwords, true);
    }

    private static UberLanguageDetector getUberLanguageInstance() {
        if (uberLanguageDetector == null) {
            uberLanguageDetector = UberLanguageDetector.getInstance();
        }
        return uberLanguageDetector;
    }

    static String detectLang(String text) {
        return getUberLanguageInstance().detectLang(text);
    }

    //Code From org.apache.commons.text.similarity. 
    /**
     * Calculates the cosine similarity for two given vectors.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @return cosine similarity between the two vectors
     */
    public static Double cosineSimilarity(Map<String, Double> leftVector, Map<String, Double> rightVector) {
        if (leftVector == null || rightVector == null) {
            throw new IllegalArgumentException("Vectors must not be null");
        }

        Set<String> intersection = getIntersection(leftVector, rightVector);

//        System.err.println(leftVector);
//        System.err.println(rightVector);
        double dotProduct = dot(leftVector, rightVector, intersection);
        double d1 = 0.0d;
        for (Double value : leftVector.values()) {
            d1 += Math.pow(value, 2);
        }
        double d2 = 0.0d;
        for (Double value : rightVector.values()) {
            d2 += Math.pow(value, 2);
        }
        double cosineSimilarity;
        if (d1 <= 0.0 || d2 <= 0.0) {
            cosineSimilarity = 0.0;
        } else {
            cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
        }
        return cosineSimilarity;
    }

    /**
     * Returns a set with strings common to the two given maps.
     *
     * @param leftVector left vector map
     * @param rightVector right vector map
     * @return common strings
     */
    private static Set<String> getIntersection(Map<String, Double> leftVector,
            Map<String, Double> rightVector) {
        Set<String> intersection = new HashSet<>(leftVector.keySet());
        intersection.retainAll(rightVector.keySet());
        return intersection;
    }

    /**
     * Computes the dot product of two vectors. It ignores remaining elements.
     * It means that if a vector is longer than other, then a smaller part of it
     * will be used to compute the dot product.
     *
     * @param leftVector left vector
     * @param rightVector right vector
     * @param intersection common elements
     * @return the dot product
     */
    private static double dot(Map<String, Double> leftVector, Map<String, Double> rightVector,
            Set<String> intersection) {
        Double dotProduct = 0.0;
        for (String key : intersection) {
            dotProduct += leftVector.get(key) * rightVector.get(key);
        }
        return dotProduct;
    }

    private static Properties getProperties() throws IOException {
        InputStream in = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            in = classLoader.getResourceAsStream(propertiesPath);
            Properties properties = new Properties();
            properties.load(in);

            return properties;
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return null;
    }

    public static int getTermLimit() throws IOException {
        return Integer.valueOf(getProperties().getProperty("term.limit", "2"));
    }

    static int getTreeDepth() throws IOException {
        return Integer.valueOf(getProperties().getProperty("tree.depth", "2"));
    }

    static int getMaxNGrams() throws IOException {
        return Integer.valueOf(getProperties().getProperty("max.ngrams", "2"));
    }

    static String getCachePath() throws IOException {
        return getProperties().getProperty("cache.path", System.getProperty("user.home")
                + File.separator + "workspace" + File.separator + "TEXT" + File.separator + "cache" + File.separator + "cacheDB");
    }

    static int getMaxPrunDepth() throws IOException {
        return Integer.valueOf(getProperties().getProperty("max.prun.depth", "4"));
    }
}
