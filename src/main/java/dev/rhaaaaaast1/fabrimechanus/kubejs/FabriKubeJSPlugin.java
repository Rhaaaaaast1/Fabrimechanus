package dev.rhaaaaaast1.fabrimechanus.kubejs;

import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.rhaaaaaast1.fabrimechanus.compat.ComposterOutput;

/** Exposes the composter output setter to KubeJS without registering an event. */
public final class FabriKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow(ComposterOutput.class);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("ComposterOutput", ComposterOutput.class);
    }
}
