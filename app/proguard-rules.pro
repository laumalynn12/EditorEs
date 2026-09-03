-keep class org.eclipse.tm4e.core.internal.theme.raw.** { *; }
-keep class org.eclipse.tm4e.core.internal.grammar.raw.** { *; }
-keep class io.github.rosemoe.sora.langs.textmate.registry.reader.** { *; }
-keep class org.eclipse.tm4e.core.internal.parser.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keep class com.termux.terminal.JNI { *; }
-keep class com.termux.terminal.** { *; }
-keep class com.editor.es.service.TermuxService { *; }
-keep class com.editor.es.storage.EditorEsDocumentsProvider { *; }
-keep class org.eclipse.lsp4j.** { *; }
-keep class io.github.rosemoe.sora.lsp.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepclassmembers enum org.eclipse.lsp4j.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-dontwarn org.eclipse.lsp4j.**
-dontwarn com.google.gson.**
-keep class com.termux.view.** { *; }
-keep class com.android.tools.smali.** { *; }
-keep class com.android.apksig.** { *; }
-keep class org.bouncycastle.** { *; }
-dontwarn com.android.tools.smali.**
-dontwarn com.android.apksig.**
-dontwarn org.bouncycastle.**
-keep class com.gaurav.avnc.** { *; }
-dontwarn com.gaurav.avnc.**
