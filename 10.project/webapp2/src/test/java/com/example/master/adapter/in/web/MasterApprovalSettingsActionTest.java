package com.example.master.adapter.in.web;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MasterApprovalSettingsActionTest {

    @Test
    void 画面に埋め込むJSONはscript要素を閉じない() {
        String json = MasterApprovalSettingsAction.toScriptJson(Map.of("name", "</script><script>alert(1)</script>"));

        assertFalse(json.contains("</"), json);
        assertEquals("{\"name\":\"<\\/script><script>alert(1)<\\/script>\"}", json);
    }
}
