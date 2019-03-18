package simulation.tools;

import java.util.Arrays;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class ChoiceOption extends Option {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2715513307634553175L;
	private final String[] choices;

	public ChoiceOption(
			final String opt,
			final String longOpt,
			final boolean hasArg,
			final String description,
			final String... choices) throws IllegalArgumentException {
		super(opt, longOpt, hasArg, description + ' ' + Arrays.toString(choices));
		this.choices = choices;
	}

	public String getChoiceValue() throws RuntimeException {
		final String value = super.getValue();
		if (value == null) {
			return value;
		}
		if (ArrayUtils.contains(choices, value)) {
			return value;
		}
		throw new RuntimeException("value mode  should be one of " + Arrays.toString(choices));
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		} else if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return new EqualsBuilder().appendSuper(super.equals(o))
				.append(choices, ((ChoiceOption) o).choices)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(choices).toHashCode();
	}
}