package com.example.aotealApp.services;

import org.springframework.stereotype.Service;

@Service
public class IosPlistService {

    // Template chuẩn của Apple cho OTA Install
    private static final String PLIST_TEMPLATE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>items</key>
                <array>
                    <dict>
                        <key>assets</key>
                        <array>
                            <dict>
                                <key>kind</key>
                                <string>software-package</string>
                                <key>url</key>
                                <string>%s</string>
                            </dict>
                        </array>
                        <key>metadata</key>
                        <dict>
                            <key>bundle-identifier</key>
                            <string>%s</string>
                            <key>bundle-version</key>
                            <string>%s</string>
                            <key>kind</key>
                            <string>software</string>
                            <key>title</key>
                            <string>%s</string>
                        </dict>
                    </dict>
                </array>
            </dict>
            </plist>
            """;

    public String generatePlist(String ipaDownloadUrl, String bundleId, String version, String appName) {
        // Thay thế các placeholder bằng dữ liệu thật
        return String.format(PLIST_TEMPLATE, ipaDownloadUrl, bundleId, version, appName);
    }
}