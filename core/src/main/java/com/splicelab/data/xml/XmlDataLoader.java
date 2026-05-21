package com.splicelab.data.xml;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;

public final class XmlDataLoader {
    private final XmlReader reader = new XmlReader();

    public XmlReader.Element loadRoot(String assetPath) throws XmlParseException {
        try {
            FileHandle fh = Gdx.files.internal(assetPath);
            if (!fh.exists()) {
                // Desktop runner doesn't automatically include android/assets.
                // Try common relative path for local dev.
                String base = System.getProperty("user.dir", "");
                FileHandle alt = Gdx.files.absolute(base + "/android/src/main/assets/" + assetPath);
                if (alt.exists()) {
                    fh = alt;
                } else {
                    // Fallback: relative path in case user.dir is odd.
                    FileHandle rel = Gdx.files.absolute("android/src/main/assets/" + assetPath);
                    if (rel.exists()) {
                        fh = rel;
                    } else {
                        throw new XmlParseException("Missing asset: " + assetPath);
                    }
                }
            }
            return reader.parse(fh);
        } catch (XmlParseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XmlParseException("Failed parsing " + assetPath, ex);
        }
    }
}
