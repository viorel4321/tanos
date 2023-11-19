package l2s.gameserver.model.entity.olympiad;

public class ValidationTask implements Runnable
{
	@Override
	public void run()
	{
		Olympiad.doValidate();
		Olympiad.init();
		OlympiadDatabase.save();
	}
}
