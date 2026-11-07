import com.structkit.app.UpdateReleaseKt;
import org.json.JSONArray;
import org.json.JSONObject;

/** JVM checks for production release parsing; Android installer still requires device validation. */
public class UpdateReleaseCheck {
    static int checks;
    static JSONObject asset(String name, long size) {
        return new JSONObject().put("name",name).put("size",size)
            .put("browser_download_url","https://github.com/DangerousKitty666/StruKit/releases/download/v7.3-A/"+name);
    }
    static JSONObject release(JSONObject... assets) {
        return new JSONObject().put("name","Revision 7.3 A").put("draft",false).put("prerelease",false).put("assets",new JSONArray(assets));
    }
    static void reject(JSONObject json) {
        try { UpdateReleaseKt.parseAppUpdate(json.toString()); }
        catch(Exception expected) { checks++;return; }
        throw new AssertionError("Accepted invalid release: "+json);
    }
    static void expectCode(JSONObject json,long code) {
        if(UpdateReleaseKt.parseAppUpdate(json.toString()).getCode()!=code)throw new AssertionError("Wrong version");checks++;
    }
    public static void main(String[] args) {
        var apk=asset("StruKit-7.3-A-vc13.apk",12345);
        expectCode(release(apk),13);
        expectCode(release(apk,asset("StruKit-7.31-A-vc14.apk",12345),asset("StruKit-Windows.exe",12345)),14);
        expectCode(release(asset("StruKit-7.3-A-vc13.apk",12345).put("digest","sha256:"+"a".repeat(64))),13);
        reject(release(apk).put("draft",true));
        reject(release(apk).put("prerelease",true));
        reject(release());
        reject(release(asset("app-debug.apk",12345)));
        reject(release(asset("StruKit-7.3-A-vc0.apk",12345)));
        reject(release(asset("StruKit-7.3-A-vc13.apk",0)));
        reject(release(asset("StruKit-7.3-A-vc13.apk",1073741825L)));
        reject(release(asset("StruKit-7.3-A-vc13.apk",12345).put("digest","sha256:bad")));
        reject(release(asset("StruKit-7.3-A-vc13.apk",12345).put("browser_download_url","http://github.com/DangerousKitty666/StruKit/releases/download/v7.3-A/update.apk")));
        reject(release(asset("StruKit-7.3-A-vc13.apk",12345).put("browser_download_url","https://github.com/other/repo/releases/download/v1/update.apk")));
        reject(release(asset("StruKit-7.3-A-vc13.apk",12345).put("browser_download_url","https://github.com.evil.example/update.apk")));
        System.out.println("Passed "+checks+" production release-parser checks.");
    }
}
