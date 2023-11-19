package l2s.gameserver.templates;

import l2s.commons.collections.MultiValueSet;

public class StatsSet extends MultiValueSet<String>
{
	private static final long serialVersionUID = -2209589233655930756L;
	public static final StatsSet EMPTY;

	public StatsSet()
	{}

	public StatsSet(final StatsSet set)
	{
		super(set);
	}

	@Override
	public StatsSet clone()
	{
		return new StatsSet(this);
	}

	static
	{
		EMPTY = new StatsSet(){
			@Override
			public Object put(final String a, final Object a2)
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
