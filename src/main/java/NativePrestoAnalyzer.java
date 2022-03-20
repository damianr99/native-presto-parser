import com.facebook.presto.sql.TreePrinter;
import com.facebook.presto.sql.analyzer.Analysis;
import com.facebook.presto.sql.analyzer.Analyzer;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.tree.Statement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import org.codehaus.jackson.map.ObjectMapper;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.ObjectHandle;
import org.graalvm.nativeimage.ObjectHandles;
import org.graalvm.nativeimage.StackValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CPointerTo;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;

/*
 TODO: two options.  Can either serialize Analysis into a JSON or Thrift object which minimizes
 the surface area we need to expose.  But requires some changes to Analysis class to support / expose a
 serialized format.  Or we could implement a new object, call the getter for each of the things that matter?
 Or we can try to expose the Analysis object over Graalvm C++ interface, and call it directly.
 (Is this different to use JNI, or does it just devolve to that?).

 This class currently shows a bit of both alternatives while we decide what is the better approach.
*/
public class NativePrestoAnalyzer {
    private static String useGson(Analysis a) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC);
                   // .excludeFieldsWithoutExposeAnnotation()
        Gson gson = gsonBuilder.create();
        return gson.toJson(a);
    }

    private static String useJackson(Analysis a) {
        ObjectMapper mapper = new ObjectMapper();
       // SimpleModule module = new SimpleModule();
       // module.addSerializer(Analysis.class, new AnalysisSerializer());
        try {
            return mapper.writeValueAsString(a);
        } catch (IOException e) {
            return e.toString();
        }
    }

    public static Analysis simpleAnalyze(String sql) {
        Analyzer analyzer = new Analyzer(
                new MockConnectorSession(),
                new MockMetadataResolver(),
                new MockFunctionManager(),
                new MockTypeManager());
        return analyzer.analyze(sql);
    }

    // Examples for memory allocation / method invocation from
    // https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.tutorial/src/com/oracle/svm/tutorial/CInterfaceTutorial.java

    @CStruct("analysis")
    interface MyAnalysisPointer extends PointerBase {
        @CField("handle")
                ObjectHandle getJavaObject();
        @CField("handle")
                ObjectHandle setJavaObject(ObjectHandle value);
    }

    @CEntryPoint(name = "analyze")
    // Main entry point for analysis.  Here we invoke the Java analyzer, and return an opaque pointer
    // to the resulting Analysis class, which can we used in subsequent callbacks to interrogate the
    // result.
    //
    // Call analyzeRelease() once you are done with the object to free memory.
    public static MyAnalysisPointer analyze(IsolateThread thread, CCharPointer sql) {
        Analysis a = simpleAnalyze(CTypeConversion.toJavaString(sql));
        ObjectHandle aHandle = ObjectHandles.getGlobal().create(a);
        MyAnalysisPointer ret = StackValue.get(MyAnalysisPointer.class);
        ret.setJavaObject(aHandle);
        return ret;
    }

    @CEntryPoint(name = "analyzeRelease")
    public static void analyzeFree(IsolateThread thread, MyAnalysisPointer ca) {
        ObjectHandle aHandle = ca.getJavaObject();
        ObjectHandles.getGlobal().destroy(aHandle);
    }

    // simple helper function to dump a statement to a string
    private static String dumpStatement(Statement stmt) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            new TreePrinter(
                    new IdentityHashMap<>(),
                    new java.io.PrintStream(buf, true, "UTF-8"))
                    .print(stmt);
        } catch (
                UnsupportedEncodingException e) {
            return e.toString();
        }
        return buf.toString();
    }

    // Demonstrate how we can invoke methods on the Analysis object.  If we go this route, we'd add a
    // full suite of getter/setters that mirror the (large) Analysis API.
    @CEntryPoint(name = "getStatement")
    public static CCharPointer getStatement(IsolateThread thread, MyAnalysisPointer ca) {
        Analysis a = ObjectHandles.getGlobal().get(ca.getJavaObject());
        String s = dumpStatement(a.getStatement());
        return CTypeConversion.toCString(s).get();
    }


    /**
     * Simple demonstration entry point.
     */
    public static void main(String[] args) {
	System.out.println("I am simpleAnalzyer");
        System.out.println(useGson(simpleAnalyze("SELECT * from orders limit 5")));
    }
}
