$ErrorActionPreference='Stop'
$root=(Get-Location).Path
$cached='C:/Users/gidro/.gradle/caches/modules-2/files-2.1'
$jars=@(Get-ChildItem $cached -Recurse -Filter '*.jar' | Where-Object { $_.FullName -notmatch '\\2\.3\.21\\' } | Select-Object -ExpandProperty FullName)
$compilerCp=($jars | Where-Object { $_ -match '\\org.jetbrains.kotlin\\|\\org.jetbrains.intellij.deps\\|\\org.jetbrains\\annotations\\|\\org.jetbrains.kotlinx\\kotlinx-coroutines-core-jvm\\' }) -join ';'
$src="$root/work/rev0788a/StruKit_Revision_0.788_A/app/src/main/java/com/structkit/app"
$argText='-no-stdlib -no-reflect -jvm-target 17 -classpath "'+$root+'/work/kotlin-stdlib.jar;'+$root+'/work/rev0788a/json-test-runtime.jar" -d "'+$root+'/work/rev0788a/price-checks.jar" "'+$src+'/PriceModels.kt" "'+$src+'/PriceParser.kt" "C:/Users/gidro/Documents/Codex/2026-11-02/referenced-chatgpt-conversation-this-is-an-2/work/rev0788a/StruKit_Revision_0.788_A/app/src/main/java/com/structkit/app/MarketPrices.kt" "C:/Users/gidro/Documents/Codex/2026-11-02/referenced-chatgpt-conversation-this-is-an-2/work/rev0788a/StruKit_Revision_0.788_A/app/src/main/java/com/structkit/app/PdfSearch.kt" "'+$root+'/work/rev0788a/PriceChecks.kt"'
Set-Content "$root/work/rev0788a/price.args" $argText.Replace('\','/')
& 'C:/Users/gidro/.jdks/jbr-21.0.11/bin/java.exe' -cp $compilerCp "$root/work/KotlinCheck.java" "@$root/work/rev0788a/price.args"
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
& 'C:/Users/gidro/.jdks/jbr-21.0.11/bin/java.exe' -cp "$root/work/rev0788a/price-checks.jar;$root/work/kotlin-stdlib.jar;$root/work/rev0788a/json-test-runtime.jar" com.structkit.app.PriceChecksKt "$root/work/rev0788a/supplier-fixtures"
exit $LASTEXITCODE


