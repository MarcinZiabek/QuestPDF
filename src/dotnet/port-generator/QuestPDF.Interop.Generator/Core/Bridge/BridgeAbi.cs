namespace QuestPDF.Interop.Generator.Core.Bridge;

/// <summary>
/// The one place that decides how a marshal expands into ABI slots. All three
/// emitters (C# exports, JNA interface, Kotlin bodies) consume this layout, so
/// the boundary stays consistent by construction.
/// </summary>
public static class BridgeAbi
{
    public static AbiSlot ScalarSlot(ScalarKind kind) => kind switch
    {
        ScalarKind.Boolean => AbiSlot.Int,
        ScalarKind.Byte or ScalarKind.UByte => AbiSlot.Byte,
        ScalarKind.Short or ScalarKind.UShort => AbiSlot.Short,
        ScalarKind.Int or ScalarKind.UInt => AbiSlot.Int,
        ScalarKind.Long or ScalarKind.ULong => AbiSlot.Long,
        ScalarKind.Float => AbiSlot.Float,
        ScalarKind.Double => AbiSlot.Double,
        _ => throw new InvalidOperationException($"Unknown scalar kind {kind}"),
    };

    /// <summary>ABI parameters one logical parameter expands to.</summary>
    public static IReadOnlyList<AbiParameter> ParameterLayout(string name, BridgeMarshal marshal) => marshal switch
    {
        BridgeMarshal.Scalar s => [new AbiParameter(name, ScalarSlot(s.Kind))],
        BridgeMarshal.NullableScalar n => [new AbiParameter(name + "HasValue", AbiSlot.Byte), new AbiParameter(name, ScalarSlot(n.Kind))],
        BridgeMarshal.EnumValue { Nullable: true } => [new AbiParameter(name + "HasValue", AbiSlot.Byte), new AbiParameter(name, AbiSlot.Int)],
        BridgeMarshal.EnumValue => [new AbiParameter(name, AbiSlot.Int)],
        BridgeMarshal.Text => [new AbiParameter(name, AbiSlot.Text)],
        BridgeMarshal.DateTimeText => [new AbiParameter(name, AbiSlot.Text)],
        BridgeMarshal.Blob => [new AbiParameter(name, AbiSlot.Bytes), new AbiParameter(name + "Length", AbiSlot.Int)],
        BridgeMarshal.Handle => [new AbiParameter(name, AbiSlot.Long)],
        BridgeMarshal.HandleSequence => [new AbiParameter(name, AbiSlot.LongArray), new AbiParameter(name + "Count", AbiSlot.Int)],
        BridgeMarshal.TextSequence => [new AbiParameter(name, AbiSlot.TextArray), new AbiParameter(name + "Count", AbiSlot.Int)],
        BridgeMarshal.ScalarArray => [new AbiParameter(name, AbiSlot.FloatArray), new AbiParameter(name + "Count", AbiSlot.Int)],
        BridgeMarshal.CallbackValue c => [new AbiParameter(name, AbiSlot.Callback, c.Shape)],
        BridgeMarshal.UserImplValue => [new AbiParameter(name, AbiSlot.Long)],
        _ => throw new InvalidOperationException($"Marshal {marshal.GetType().Name} cannot appear as a parameter"),
    };

    /// <summary>
    /// ABI slot of a return value. <see cref="AbiSlot.BufferOut"/> means the
    /// export returns void and appends buffer out-parameters instead.
    /// </summary>
    public static AbiSlot ReturnLayout(BridgeMarshal marshal) => marshal switch
    {
        BridgeMarshal.Nothing => AbiSlot.Void,
        BridgeMarshal.Scalar s => ScalarSlot(s.Kind),
        // Nullable scalars/enums return the value and append a has-value out-parameter.
        BridgeMarshal.NullableScalar n => ScalarSlot(n.Kind),
        BridgeMarshal.EnumValue => AbiSlot.Int,
        BridgeMarshal.Text => AbiSlot.TextReturn,
        BridgeMarshal.DateTimeText => AbiSlot.TextReturn,
        BridgeMarshal.Blob => AbiSlot.BufferOut,
        BridgeMarshal.Handle => AbiSlot.Long,
        _ => throw new InvalidOperationException($"Marshal {marshal.GetType().Name} cannot appear as a return value"),
    };

    /// <summary>
    /// Slots of a callback's logical parameter as seen by the callback ABI.
    /// Text is allowed inbound (the C# side passes a temporary UTF-8 pointer);
    /// handles/blobs/text returned FROM a callback travel as registered handles.
    /// </summary>
    public static IReadOnlyList<AbiSlot> CallbackParameterSlots(BridgeMarshal marshal) => marshal switch
    {
        BridgeMarshal.Scalar s => [ScalarSlot(s.Kind)],
        BridgeMarshal.NullableScalar n => [AbiSlot.Byte, ScalarSlot(n.Kind)],
        BridgeMarshal.EnumValue { Nullable: true } => [AbiSlot.Byte, AbiSlot.Int],
        BridgeMarshal.EnumValue => [AbiSlot.Int],
        BridgeMarshal.Text => [AbiSlot.Text],
        BridgeMarshal.Handle => [AbiSlot.Long],
        _ => throw new BridgePlanningException($"a {marshal.GetType().Name} value cannot cross into a callback"),
    };

    /// <summary>Slot carrying a callback's return value back to .NET.</summary>
    public static AbiSlot CallbackReturnSlot(BridgeMarshal marshal) => marshal switch
    {
        BridgeMarshal.Nothing => AbiSlot.Void,
        BridgeMarshal.Scalar s => ScalarSlot(s.Kind),
        BridgeMarshal.EnumValue => AbiSlot.Int,
        BridgeMarshal.Handle => AbiSlot.Long,
        // Strings and buffers produced by Kotlin callbacks come back as
        // registered transfer handles the C# side takes ownership of.
        BridgeMarshal.Text => AbiSlot.Long,
        BridgeMarshal.Blob => AbiSlot.Long,
        _ => throw new BridgePlanningException($"a {marshal.GetType().Name} value cannot be returned from a callback"),
    };

    /// <summary>Returns crossing as value + has-value out-parameter (nullable scalars and enums).</summary>
    public static bool IsFlaggedReturn(BridgeMarshal marshal) =>
        marshal is BridgeMarshal.NullableScalar or BridgeMarshal.EnumValue { Nullable: true };

    public static CallbackShape ShapeFor(IReadOnlyList<BridgeMarshal> parameterWraps, BridgeMarshal returnWrap)
    {
        var slots = parameterWraps.SelectMany(CallbackParameterSlots).ToList();
        var returnSlot = CallbackReturnSlot(returnWrap);
        var name = "NativeCallback" +
                   (slots.Count == 0 ? "0" : string.Concat(slots.Select(SlotLetter))) +
                   "To" + SlotLetter(returnSlot);
        return new CallbackShape(name, slots, returnSlot);
    }

    private static string SlotLetter(AbiSlot slot) => slot switch
    {
        AbiSlot.Byte => "B",
        AbiSlot.Short => "H",
        AbiSlot.Int => "I",
        AbiSlot.Long => "J",
        AbiSlot.Float => "F",
        AbiSlot.Double => "D",
        AbiSlot.Text => "S",
        AbiSlot.Void => "V",
        _ => throw new InvalidOperationException($"Slot {slot} cannot appear in a callback shape"),
    };
}
