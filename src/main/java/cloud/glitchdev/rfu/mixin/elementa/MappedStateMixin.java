package cloud.glitchdev.rfu.mixin.elementa;

import cloud.glitchdev.rfu.config.categories.OtherSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.essential.elementa.state.BasicState;
import gg.essential.elementa.state.MappedState;
import gg.essential.elementa.state.State;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;

@Mixin(value = MappedState.class, remap = false)
public abstract class MappedStateMixin<T, U> extends BasicState<U> {
    @Shadow @Final private Function1<T, U> mapper;

    public MappedStateMixin(U valueBacker) {
        super(valueBacker);
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(
        method = {"<init>", "rebind"},
        at = @At(value = "INVOKE", target = "Lgg/essential/elementa/state/State;onSetValue(Lkotlin/jvm/functions/Function1;)Lkotlin/jvm/functions/Function0;")
    )
    private Function0<Unit> rfu$wrapOnSetValue(State<T> state, Function1<T, Unit> listener, Operation<Function0<Unit>> original) {
        if (!OtherSettings.INSTANCE.getPatchElementaMemoryLeaks()) {
            return original.call(state, listener);
        }

        WeakReference<MappedState<T, U>> weakRef = new WeakReference<>((MappedState<T, U>) (Object) this);
        Function1<T, U> localMapper = this.mapper;

        Function1<T, Unit> weakListener = value -> {
            MappedState<T, U> mappedState = weakRef.get();
            if (mappedState != null) {
                mappedState.set(localMapper.invoke(value));
            }
            return Unit.INSTANCE;
        };

        return original.call(state, weakListener);
    }
}
