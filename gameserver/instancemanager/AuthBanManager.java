package l2s.gameserver.instancemanager;

import l2s.commons.ban.BanManager;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 * Менеджер для кеширования банов логин-сервера
 **/
public class AuthBanManager extends BanManager {
	private static final AuthBanManager INSTANCE = new AuthBanManager();

	public static AuthBanManager getInstance() {
		return INSTANCE;
	}
}
