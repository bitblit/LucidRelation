package com.erigir.lucid.modifier;

import java.util.List;

/**
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:41 PM
 */
public class CompoundScanAndReplace implements IScanAndReplace {
    private List<SingleScanAndReplace> scanList;

    public CompoundScanAndReplace() {
    }

    public CompoundScanAndReplace(List<SingleScanAndReplace> scanList) {
        this.scanList = scanList;
    }

    @Override
    public String performScanAndReplace(String value) {
        String rval = value;
        if (rval!=null && scanList!=null)
        {
            for (SingleScanAndReplace s:scanList)
            {
                rval = s.performScanAndReplace(rval);
            }
        }
        return rval;
    }

    public void setScanList(List<SingleScanAndReplace> scanList) {
        this.scanList = scanList;
    }
}
