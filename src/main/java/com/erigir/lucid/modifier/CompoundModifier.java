package com.erigir.lucid.modifier;

import java.util.List;

/**
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:41 PM
 */
public class CompoundModifier implements IStringModifier {
    private List<? extends IStringModifier> modifierList;

    public CompoundModifier() {
    }

    public CompoundModifier(List<IStringModifier> modifierList) {
        this.modifierList = modifierList;
    }

    @Override
    public String modify(String body) {
        String rval = body;
        if (rval!=null && modifierList!=null)
        {
            for (IStringModifier s:modifierList)
            {
                rval = s.modify(rval);
            }
        }
        return rval;
    }

    public void setModifierList(List<IStringModifier> modifierList) {
        this.modifierList = modifierList;
    }
}
