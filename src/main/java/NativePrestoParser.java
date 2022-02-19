import com.facebook.presto.sql.TreePrinter;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.Statement;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.IdentityHashMap;

/**
 * Simple demonstration of providing a C-based API to the Presto Parser using Graalvm native image.
 */
public class NativePrestoParser {
    private static String simpleParse(String sql) {
        SqlParser parser = new SqlParser();
        Statement stmt;
        try {
             stmt = parser.createStatement(sql, ParsingOptions.builder().build());
        } catch (Exception e) {
            return e.toString();
        }
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            new TreePrinter(
                    new IdentityHashMap<>(),
                    new java.io.PrintStream(buf, true, "UTF-8"))
                    .print(stmt);
        } catch (UnsupportedEncodingException e) {
            return e.toString();
        }
        return buf.toString();
    }

    /**
     * TODO: this simple demonstration just returns the parsed statement as a string.
     * For a useful parser, we need to generate an AST that can be used on the C side.
     * For that, we can generate a tree of C structs that are equivalent to Presto classes.
     * One way of doing that would be to walk the AST using AstVisitor much like the
     * presto.sql.TreePrinter class does.
     * Example of returning a tree of C-structs here:
     * https://github.com/michael-simons/neo4j-java-driver-native-lib/blob/master/src/main/java/org/neo4j/examples/drivernative/DriverNativeLib.java
     */
    @CEntryPoint(name = "parse")
    public static CCharPointer parse(IsolateThread thread, CCharPointer sql) {
        String parse = simpleParse(CTypeConversion.toJavaString(sql));
        return CTypeConversion.toCString(parse).get();
    }

    /**
     * Simple demonstration entry point.
     */
    public static void main(String[] args) {
        System.out.println(simpleParse("SELECT foo1(foo2(x)+2)/3,MAX(z) from xyz order by foo limit 5"));
    }
}
