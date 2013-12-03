package com.erigir.lucid.modifier;

import java.util.List;

/**
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:41 PM
 */
public class CompoundScanAndReplace implements IStringModifier {
    private List<ScanAndReplace> scanList;

    public CompoundScanAndReplace() {
    }

    public CompoundScanAndReplace(List<ScanAndReplace> scanList) {
        this.scanList = scanList;
    }

    @Override
    public String modify(String body) {
        String rval = body;
        if (rval!=null && scanList!=null)
        {
            for (ScanAndReplace s:scanList)
            {
                rval = s.performScanAndReplace(rval);
            }
        }
        return rval;
    }

    public void setScanList(List<ScanAndReplace> scanList) {
        this.scanList = scanList;
    }
}
