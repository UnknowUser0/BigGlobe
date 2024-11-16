package builderb0y.bigglobe.scripting.wrappers.tags;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.function.Consumer;

import builderb0y.bigglobe.scripting.ScriptLogger;
import builderb0y.scripting.bytecode.MethodInfo;
import builderb0y.scripting.bytecode.TypeInfo;
import builderb0y.scripting.bytecode.tree.ConstantValue;
import builderb0y.scripting.bytecode.tree.InsnTree;
import builderb0y.scripting.bytecode.tree.InsnTree.CastMode;
import builderb0y.scripting.environments.MutableScriptEnvironment;
import builderb0y.scripting.environments.MutableScriptEnvironment.CastHandler;
import builderb0y.scripting.environments.MutableScriptEnvironment.CastResult;
import builderb0y.scripting.environments.MutableScriptEnvironment.KeywordHandler;
import builderb0y.scripting.environments.MutableScriptEnvironment.MethodHandler;
import builderb0y.scripting.environments.ScriptEnvironment;
import builderb0y.scripting.environments.ScriptEnvironment.GetMethodMode;
import builderb0y.scripting.parsing.ExpressionParser;
import builderb0y.scripting.parsing.ScriptParsingException;
import builderb0y.scripting.parsing.special.CommaSeparatedExpressions;
import builderb0y.scripting.util.TypeInfos;

import static builderb0y.scripting.bytecode.InsnTrees.*;

public class TagParser implements Consumer<MutableScriptEnvironment> {

	public final String tagTypeName, elementTypeName;
	public final TypeInfo tagType, elementType;
	public final MethodInfo bootstrapConstant, nonConstant, isIn;

	public TagParser(String tagTypeName, Class<?> tagClass, String elementTypeName, MethodInfo isIn) {
		this.tagTypeName = tagTypeName;
		this.elementTypeName = elementTypeName;
		this.tagType = isIn.getInvokeTypes()[1];
		this.elementType = isIn.getInvokeTypes()[0];
		this.bootstrapConstant = MethodInfo.findMethod(tagClass, "of", tagClass, MethodHandles.Lookup.class, String.class, Class.class, String[].class);
		this.nonConstant = MethodInfo.findMethod(tagClass, "of", tagClass, String[].class);
		this.isIn = isIn;
	}

	@Override
	public void accept(MutableScriptEnvironment environment) {
		environment
		.addCast(type(String.class), this.tagType, true, this.makeCaster())
		.addKeyword(this.tagTypeName, this.makeKeyword())
		.addMethod(this.elementType, "isIn", this.makeIsIn());
	}

	public CastHandler.Named makeCaster() {
		return new CastHandler.Named(
			"String -> " + this.tagTypeName,
			(ExpressionParser parser, InsnTree value, TypeInfo to, boolean implicit) -> {
				if (value.getConstantValue().isConstant()) {
					return ldc(
						this.bootstrapConstant,
						value.getConstantValue()
					);
				}
				else {
					if (implicit) {
						ScriptLogger.LOGGER.warn("Non-constant tag; this will be worse on performance. Use an explicit cast to suppress this warning. " + ScriptParsingException.appendContext(parser.input));
					}
					return invokeStatic(
						this.nonConstant,
						newArrayWithContents(parser, type(String[].class), value)
					);
				}
			}
		);
	}

	public KeywordHandler.Named makeKeyword() {
		return new KeywordHandler.Named(
			this.tagTypeName + "(element1 [, element2, ...])",
			(ExpressionParser parser, String name) -> {
				if (parser.input.peekAfterWhitespace() != '(') return null;
				CommaSeparatedExpressions expressions = CommaSeparatedExpressions.parse(parser);
				return switch (expressions.arguments().length) {
					case 0 -> throw new ScriptParsingException("At least one element is required", parser.input);
					case 1 -> expressions.maybeWrap(expressions.arguments()[0].cast(parser, this.tagType, CastMode.EXPLICIT_THROW));
					default -> {
						InsnTree[] strings = Arrays.stream(expressions.arguments()).map((InsnTree tree) -> tree.cast(parser, TypeInfos.STRING, CastMode.IMPLICIT_THROW)).toArray(InsnTree[]::new);
						if (Arrays.stream(strings).map(InsnTree::getConstantValue).allMatch(ConstantValue::isConstantOrDynamic)) {
							yield ldc(
								this.bootstrapConstant,
								Arrays.stream(strings).map(InsnTree::getConstantValue).toArray(ConstantValue[]::new)
							);
						}
						else {
							yield invokeStatic(
								this.nonConstant,
								newArrayWithContents(parser, type(String[].class), strings)
							);
						}
					}
				};
			}
		);
	}

	public MethodHandler.Named makeIsIn() {
		return new MethodHandler.Named(
			this.elementTypeName + ".isIn(element1 [, element2, ...])",
			(
				ExpressionParser parser,
				InsnTree receiver,
				String name,
				GetMethodMode mode,
				InsnTree... arguments
			)
			-> {
				InsnTree tagArgument;
				boolean needsCasting;
				switch (arguments.length) {
					case 0 -> throw new ScriptParsingException("At least one argument is required", parser.input);
					case 1 -> {
						tagArgument = arguments[0].cast(parser, this.tagType, CastMode.EXPLICIT_THROW);
						needsCasting = tagArgument != arguments[0];
					}
					default -> {
						InsnTree[] strings = ScriptEnvironment.castArgumentsSameType(parser, "isIn", TypeInfos.STRING, CastMode.IMPLICIT_THROW, arguments);
						if (strings == null) return null;
						if (Arrays.stream(strings).map(InsnTree::getConstantValue).allMatch(ConstantValue::isConstantOrDynamic)) {
							tagArgument = ldc(
								this.bootstrapConstant,
								Arrays.stream(strings).map(InsnTree::getConstantValue).toArray(ConstantValue[]::new)
							);
						}
						else {
							tagArgument = invokeStatic(
								this.nonConstant,
								newArrayWithContents(parser, type(String[].class), strings)
							);
						}
						needsCasting = strings != arguments;
					}
				}

				return new CastResult(
					this.isIn.isStatic()
					? invokeStatic(this.isIn, receiver, tagArgument)
					: invokeInstance(receiver, this.isIn, tagArgument),
					needsCasting
				);
			}
		);
	}
}