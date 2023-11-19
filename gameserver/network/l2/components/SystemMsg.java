package l2s.gameserver.network.l2.components;

import java.util.NoSuchElementException;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author VISTALL
 * @date  13:28/01.12.2010
 */
public enum SystemMsg implements IBroadcastPacket
{	// Message: You have been disconnected from the server.
	YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER(0),
	// Message: The server will be coming down in $s1 second(s).  Please find a safe place to log out.
	THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS__PLEASE_FIND_A_SAFE_PLACE_TO_LOG_OUT(1),
	// Message: $s1 does not exist.
	S1_DOES_NOT_EXIST(2),
	// Message: $s1 is not currently logged in.
	S1_IS_NOT_CURRENTLY_LOGGED_IN(3),
	// Message: You cannot ask yourself to apply to a clan.
	YOU_CANNOT_ASK_YOURSELF_TO_APPLY_TO_A_CLAN(4),
	// Message: $s1 already exists.
	S1_ALREADY_EXISTS(5),
	// Message: $s1 does not exist.
	S1_DOES_NOT_EXIST_(6),
	// Message: You are already a member of $s1.
	YOU_ARE_ALREADY_A_MEMBER_OF_S1(7),
	// Message: You are working with another clan.
	YOU_ARE_WORKING_WITH_ANOTHER_CLAN(8),
	// Message: $s1 is not a clan leader.
	S1_IS_NOT_A_CLAN_LEADER(9),
	// Message: $s1 is working with another clan.
	S1_IS_WORKING_WITH_ANOTHER_CLAN(10),
	// Message: There are no applicants for this clan.
	THERE_ARE_NO_APPLICANTS_FOR_THIS_CLAN(11),
	// Message: Applicant information is incorrect.
	APPLICANT_INFORMATION_IS_INCORRECT(12),
	// Message: Unable to disperse: your clan has requested to participate in a castle siege.
	UNABLE_TO_DISPERSE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE(13),
	// Message: Unable to disperse: your clan owns one or more castles or hideouts.
	UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS(14),
	// Message: You are in siege.
	YOU_ARE_IN_SIEGE(15),
	// Message: You are not in siege.
	YOU_ARE_NOT_IN_SIEGE(16),
	// Message: The castle siege has begun.
	THE_CASTLE_SIEGE_HAS_BEGUN(17),
	// Message: The castle siege has ended.
	THE_CASTLE_SIEGE_HAS_ENDED(18),
	// Message: There is a new Lord of the castle!
	THERE_IS_A_NEW_LORD_OF_THE_CASTLE(19),
	// Message: The gate is being opened.
	THE_GATE_IS_BEING_OPENED(20),
	// Message: The gate is being destroyed.
	THE_GATE_IS_BEING_DESTROYED(21),
	// Message: Your target is out of range.
	YOUR_TARGET_IS_OUT_OF_RANGE(22),
	// Message: Not enough HP.
	NOT_ENOUGH_HP(23),
	// Message: Not enough MP.
	NOT_ENOUGH_MP(24),
	// Message: Rejuvenating HP.
	REJUVENATING_HP(25),
	// Message: Rejuvenating MP.
	REJUVENATING_MP(26),
	// Message: Your casting has been interrupted.
	YOUR_CASTING_HAS_BEEN_INTERRUPTED(27),
	// Message: You have obtained $s1 adena.
	YOU_HAVE_OBTAINED_S1_ADENA(28),
	// Message: You have obtained $s2 $s1.
	YOU_HAVE_OBTAINED_S2_S1(29),
	// Message: You have obtained $s1.
	YOU_HAVE_OBTAINED_S1(30),
	// Message: You cannot move while sitting.
	YOU_CANNOT_MOVE_WHILE_SITTING(31),
	// Message: You are unable to engage in combat. Please go to the nearest restart point.
	YOU_ARE_UNABLE_TO_ENGAGE_IN_COMBAT_PLEASE_GO_TO_THE_NEAREST_RESTART_POINT(32),
	// Message: You cannot move while casting.
	YOU_CANNOT_MOVE_WHILE_CASTING(33),
	// Message: Welcome to the World of Lineage II.
	WELCOME_TO_THE_WORLD_OF_LINEAGE_II(34),
	// Message: You hit for $s1 damage.
	YOU_HIT_FOR_S1_DAMAGE(35),
	// Message: $s1 hit you for $s2 damage.
	S1_HIT_YOU_FOR_S2_DAMAGE(36),
	// Message: $s1 hit you for $s2 damage.
	S1_HIT_YOU_FOR_S2_DAMAGE_(37),
	// Message: The TGS2002 event begins!
	THE_TGS2002_EVENT_BEGINS(38),
	// Message: The TGS2002 event is over. Thank you very much.
	THE_TGS2002_EVENT_IS_OVER_THANK_YOU_VERY_MUCH(39),
	// Message: This is the TGS demo: the character will immediately be restored.
	THIS_IS_THE_TGS_DEMO_THE_CHARACTER_WILL_IMMEDIATELY_BE_RESTORED(40),
	// Message: You carefully nock an arrow…
	YOU_CAREFULLY_NOCK_AN_ARROW(41),
	// Message: You have avoided $s1's attack.
	YOU_HAVE_AVOIDED_S1S_ATTACK(42),
	// Message: You have missed.
	YOU_HAVE_MISSED(43),
	// Message: Critical hit!
	CRITICAL_HIT(44),
	// Message: You have earned $s1 experience.
	YOU_HAVE_EARNED_S1_EXPERIENCE(45),
	// Message: You use $s1.
	YOU_USE_S1(46),
	// Message: You begin to use a(n) $s1.
	YOU_BEGIN_TO_USE_AN_S1(47),
	// Message: $s1 is not available at this time: being prepared for reuse.
	S1_IS_NOT_AVAILABLE_AT_THIS_TIME_BEING_PREPARED_FOR_REUSE(48),
	// Message: You have equipped your $s1.
	YOU_HAVE_EQUIPPED_YOUR_S1(49),
	// Message: Your target cannot be found.
	YOUR_TARGET_CANNOT_BE_FOUND(50),
	// Message: You cannot use this on yourself.
	YOU_CANNOT_USE_THIS_ON_YOURSELF(51),
	// Message: You have earned $s1 adena.
	YOU_HAVE_EARNED_S1_ADENA(52),
	// Message: You have earned $s2 $s1(s).
	YOU_HAVE_EARNED_S2_S1S(53),
	// Message: You have earned $s1.
	YOU_HAVE_EARNED_S1(54),
	// Message: You have failed to pick up $s1 adena.
	YOU_HAVE_FAILED_TO_PICK_UP_S1_ADENA(55),
	// Message: You have failed to pick up $s1.
	YOU_HAVE_FAILED_TO_PICK_UP_S1(56),
	// Message: You have failed to pick up $s2 $s1(s).
	YOU_HAVE_FAILED_TO_PICK_UP_S2_S1S(57),
	// Message: You have failed to earn $s1 adena.
	YOU_HAVE_FAILED_TO_EARN_S1_ADENA(58),
	// Message: You have failed to earn $s1.
	YOU_HAVE_FAILED_TO_EARN_S1(59),
	// Message: You have failed to earn $s2 $s1(s).
	YOU_HAVE_FAILED_TO_EARN_S2_S1S(60),
	// Message: Nothing happened.
	NOTHING_HAPPENED(61),
	// Message: Your $s1 has been successfully enchanted.
	YOUR_S1_HAS_BEEN_SUCCESSFULLY_ENCHANTED(62),
	// Message: Your +$S1 $S2 has been successfully enchanted.
	YOUR_S1_S2_HAS_BEEN_SUCCESSFULLY_ENCHANTED(63),
	// Message: The enchantment has failed!  Your $s1 has been crystallized.
	THE_ENCHANTMENT_HAS_FAILED__YOUR_S1_HAS_BEEN_CRYSTALLIZED(64),
	// Message: The enchantment has failed!  Your +$s1 $s2 has been crystallized.
	THE_ENCHANTMENT_HAS_FAILED__YOUR_S1_S2_HAS_BEEN_CRYSTALLIZED(65),
	// Message: $s1 has invited you to his/her party. Do you accept the invitation?
	S1_HAS_INVITED_YOU_TO_HISHER_PARTY_DO_YOU_ACCEPT_THE_INVITATION(66),
	// Message: $s1 has invited you to the join the clan, $s2. Do you wish to join?
	S1_HAS_INVITED_YOU_TO_THE_JOIN_THE_CLAN_S2_DO_YOU_WISH_TO_JOIN(67),
	// Message: Would you like to withdraw from the $s1 clan? If you leave, you will have to wait at least a day before joining another clan.
	WOULD_YOU_LIKE_TO_WITHDRAW_FROM_THE_S1_CLAN_IF_YOU_LEAVE_YOU_WILL_HAVE_TO_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN(68),
	// Message: Would you like to dismiss $s1 from the clan? If you do so, you will have to wait at least a day before accepting a new member.
	WOULD_YOU_LIKE_TO_DISMISS_S1_FROM_THE_CLAN_IF_YOU_DO_SO_YOU_WILL_HAVE_TO_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER(69),
	// Message: Do you wish to disperse the clan, $s1?
	DO_YOU_WISH_TO_DISPERSE_THE_CLAN_S1(70),
	// Message: How many of your $s1(s) do you wish to discard?
	HOW_MANY_OF_YOUR_S1S_DO_YOU_WISH_TO_DISCARD(71),
	// Message: How many of your $s1(s) do you wish to move?
	HOW_MANY_OF_YOUR_S1S_DO_YOU_WISH_TO_MOVE(72),
	// Message: How many of your $s1(s) do you wish to destroy?
	HOW_MANY_OF_YOUR_S1S_DO_YOU_WISH_TO_DESTROY(73),
	// Message: Do you wish to destroy your $s1?
	DO_YOU_WISH_TO_DESTROY_YOUR_S1(74),
	// Message: ID does not exist.
	ID_DOES_NOT_EXIST(75),
	// Message: Incorrect password.
	INCORRECT_PASSWORD(76),
	// Message: You cannot create another character. Please delete the existing character and try again.
	YOU_CANNOT_CREATE_ANOTHER_CHARACTER_PLEASE_DELETE_THE_EXISTING_CHARACTER_AND_TRY_AGAIN(77),
	// Message: Do you wish to delete $s1?
	DO_YOU_WISH_TO_DELETE_S1(78),
	// Message: This name already exists.
	THIS_NAME_ALREADY_EXISTS(79),
	// Message: Names must be between 1-16 characters, excluding spaces or special characters.
	NAMES_MUST_BE_BETWEEN_116_CHARACTERS_EXCLUDING_SPACES_OR_SPECIAL_CHARACTERS(80),
	// Message: Please select your race.
	PLEASE_SELECT_YOUR_RACE(81),
	// Message: Please select your occupation.
	PLEASE_SELECT_YOUR_OCCUPATION(82),
	// Message: Please select your gender.
	PLEASE_SELECT_YOUR_GENDER(83),
	// Message: You may not attack in a peaceful zone.
	YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE(84),
	// Message: You may not attack this target in a peaceful zone.
	YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE(85),
	// Message: Please enter your ID.
	PLEASE_ENTER_YOUR_ID(86),
	// Message: Please enter your password.
	PLEASE_ENTER_YOUR_PASSWORD(87),
	// Message: Your protocol version is different, please restart your client and run a full check.
	YOUR_PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_RESTART_YOUR_CLIENT_AND_RUN_A_FULL_CHECK(88),
	// Message: Your protocol version is different, please continue.
	YOUR_PROTOCOL_VERSION_IS_DIFFERENT_PLEASE_CONTINUE(89),
	// Message: You are unable to connect to the server.
	YOU_ARE_UNABLE_TO_CONNECT_TO_THE_SERVER(90),
	// Message: Please select your hairstyle.
	PLEASE_SELECT_YOUR_HAIRSTYLE(91),
	// Message: $s1 has worn off.
	S1_HAS_WORN_OFF(92),
	// Message: You do not have enough SP for this.
	YOU_DO_NOT_HAVE_ENOUGH_SP_FOR_THIS(93),
	// Message: 2002 - 2007 Copyright NCsoft Corporation. All Rights Reserved.
	_2002__2007_COPYRIGHT_NCSOFT_CORPORATION_ALL_RIGHTS_RESERVED(94),
	// Message: You have earned $s1 experience and $s2 SP.
	YOU_HAVE_EARNED_S1_EXPERIENCE_AND_S2_SP(95),
	// Message: Your level has increased!
	YOUR_LEVEL_HAS_INCREASED(96),
	// Message: This item cannot be moved.
	THIS_ITEM_CANNOT_BE_MOVED(97),
	// Message: This item cannot be discarded.
	THIS_ITEM_CANNOT_BE_DISCARDED(98),
	// Message: This item cannot be traded or sold.
	THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD(99),
	// Message: $s1 has requested a trade. Do you wish to continue?
	S1_HAS_REQUESTED_A_TRADE_DO_YOU_WISH_TO_CONTINUE(100),
	// Message: You cannot exit while in combat.
	YOU_CANNOT_EXIT_WHILE_IN_COMBAT(101),
	// Message: You cannot restart while in combat.
	YOU_CANNOT_RESTART_WHILE_IN_COMBAT(102),
	// Message: This ID is currently logged in.
	THIS_ID_IS_CURRENTLY_LOGGED_IN(103),
	// Message: You may not equip items while casting or performing a skill.
	YOU_MAY_NOT_EQUIP_ITEMS_WHILE_CASTING_OR_PERFORMING_A_SKILL(104),
	// Message: You have invited $s1 to your party.
	YOU_HAVE_INVITED_S1_TO_YOUR_PARTY(105),
	// Message: You have joined $s1's party.
	YOU_HAVE_JOINED_S1S_PARTY(106),
	// Message: $s1 has joined the party.
	S1_HAS_JOINED_THE_PARTY(107),
	// Message: $s1 has left the party.
	S1_HAS_LEFT_THE_PARTY(108),
	// Message: Invalid target.
	INVALID_TARGET(109),
	// Message: The effects of $s1 flow through you.
	THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU(110),
	// Message: Your shield defense has succeeded.
	YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED(111),
	// Message: You have run out of arrows.
	YOU_HAVE_RUN_OUT_OF_ARROWS(112),
	// Message: $s1 cannot be used due to unsuitable terms.
	S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS(113),
	// Message: You have entered the shadow of the Mother Tree.
	YOU_HAVE_ENTERED_THE_SHADOW_OF_THE_MOTHER_TREE(114),
	// Message: You have left the shadow of the Mother Tree.
	YOU_HAVE_LEFT_THE_SHADOW_OF_THE_MOTHER_TREE(115),
	// Message: You have entered a peaceful zone.
	YOU_HAVE_ENTERED_A_PEACEFUL_ZONE(116),
	// Message: You have left the peaceful zone.
	YOU_HAVE_LEFT_THE_PEACEFUL_ZONE(117),
	// Message: You have requested a trade with $s1.
	YOU_HAVE_REQUESTED_A_TRADE_WITH_S1(118),
	// Message: $s1 has denied your request to trade.
	S1_HAS_DENIED_YOUR_REQUEST_TO_TRADE(119),
	// Message: You begin trading with $s1.
	YOU_BEGIN_TRADING_WITH_S1(120),
	// Message: $s1 has confirmed the trade.
	S1_HAS_CONFIRMED_THE_TRADE(121),
	// Message: You may no longer adjust items in the trade because the trade has been confirmed.
	YOU_MAY_NO_LONGER_ADJUST_ITEMS_IN_THE_TRADE_BECAUSE_THE_TRADE_HAS_BEEN_CONFIRMED(122),
	// Message: Your trade is successful.
	YOUR_TRADE_IS_SUCCESSFUL(123),
	// Message: $s1 has canceled the trade.
	S1_HAS_CANCELED_THE_TRADE(124),
	// Message: Do you wish to exit the game?
	DO_YOU_WISH_TO_EXIT_THE_GAME(125),
	// Message: Do you wish to exit to the character select screen?
	DO_YOU_WISH_TO_EXIT_TO_THE_CHARACTER_SELECT_SCREEN(126),
	// Message: You have been disconnected from the server. Please login again.
	YOU_HAVE_BEEN_DISCONNECTED_FROM_THE_SERVER_PLEASE_LOGIN_AGAIN(127),
	// Message: Your character creation has failed.
	YOUR_CHARACTER_CREATION_HAS_FAILED(128),
	// Message: Your inventory is full.
	YOUR_INVENTORY_IS_FULL(129),
	// Message: Your warehouse is full.
	YOUR_WAREHOUSE_IS_FULL(130),
	// Message: $s1 has logged in.
	S1_HAS_LOGGED_IN(131),
	// Message: $s1 has been added to your friends list.
	S1_HAS_BEEN_ADDED_TO_YOUR_FRIENDS_LIST(132),
	// Message: $s1 has been removed from your friends list.
	S1_HAS_BEEN_REMOVED_FROM_YOUR_FRIENDS_LIST(133),
	// Message: Please check your friends list again.
	PLEASE_CHECK_YOUR_FRIENDS_LIST_AGAIN(134),
	// Message: $s1 did not reply to your invitation; your invite has been canceled.
	S1_DID_NOT_REPLY_TO_YOUR_INVITATION_YOUR_INVITE_HAS_BEEN_CANCELED(135),
	// Message: You have not replied to $s1's invitation; the offer has been canceled.
	YOU_HAVE_NOT_REPLIED_TO_S1S_INVITATION_THE_OFFER_HAS_BEEN_CANCELED(136),
	// Message: There are no more items in the shortcut.
	THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT(137),
	// Message: Designate shortcut.
	DESIGNATE_SHORTCUT(138),
	// Message: $s1 has resisted your $s2.
	S1_HAS_RESISTED_YOUR_S2(139),
	// Message: Your skill was removed due to a lack of MP.
	YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP(140),
	// Message: Once the trade is confirmed, the item cannot be moved again.
	ONCE_THE_TRADE_IS_CONFIRMED_THE_ITEM_CANNOT_BE_MOVED_AGAIN(141),
	// Message: You are already trading with someone.
	YOU_ARE_ALREADY_TRADING_WITH_SOMEONE(142),
	// Message: $s1 is already trading with another person. Please try again later.
	S1_IS_ALREADY_TRADING_WITH_ANOTHER_PERSON_PLEASE_TRY_AGAIN_LATER(143),
	// Message: That is the incorrect target.
	THAT_IS_THE_INCORRECT_TARGET(144),
	// Message: That player is not online.
	THAT_PLAYER_IS_NOT_ONLINE(145),
	// Message: Chatting is now permitted.
	CHATTING_IS_NOW_PERMITTED(146),
	// Message: Chatting is currently prohibited.
	CHATTING_IS_CURRENTLY_PROHIBITED(147),
	// Message: You cannot use quest items.
	YOU_CANNOT_USE_QUEST_ITEMS(148),
	// Message: You cannot pick up or use items while trading.
	YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING(149),
	// Message: You cannot discard or destroy an item while trading at a private store.
	YOU_CANNOT_DISCARD_OR_DESTROY_AN_ITEM_WHILE_TRADING_AT_A_PRIVATE_STORE(150),
	// Message: That is too far from you to discard.
	THAT_IS_TOO_FAR_FROM_YOU_TO_DISCARD(151),
	// Message: You have invited the wrong target.
	YOU_HAVE_INVITED_THE_WRONG_TARGET(152),
	// Message: $s1 is busy. Please try again later.
	S1_IS_BUSY_PLEASE_TRY_AGAIN_LATER(153),
	// Message: Only the leader can give out invitations.
	ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS(154),
	// Message: The party is full.
	THE_PARTY_IS_FULL(155),
	// Message: Drain was only 50 percent successful.
	DRAIN_WAS_ONLY_50_PERCENT_SUCCESSFUL(156),
	// Message: You resisted $s1's drain.
	YOU_RESISTED_S1S_DRAIN(157),
	// Message: Your attack has failed.
	YOUR_ATTACK_HAS_FAILED(158),
	// Message: You have resisted $s1's magic.
	YOU_HAVE_RESISTED_S1S_MAGIC(159),
	// Message: $s1 is a member of another party and cannot be invited.
	S1_IS_A_MEMBER_OF_ANOTHER_PARTY_AND_CANNOT_BE_INVITED(160),
	// Message: That player is not currently online.
	THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE(161),
	// Message: Warehouse is too far.
	WAREHOUSE_IS_TOO_FAR(162),
	// Message: You cannot destroy it because the number is incorrect.
	YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT(163),
	// Message: Waiting for another reply.
	WAITING_FOR_ANOTHER_REPLY(164),
	// Message: You cannot add yourself to your own friend list.
	YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST(165),
	// Message: Friend list is not ready yet. Please register again later.
	FRIEND_LIST_IS_NOT_READY_YET_PLEASE_REGISTER_AGAIN_LATER(166),
	// Message: $s1 is already on your friend list.
	S1_IS_ALREADY_ON_YOUR_FRIEND_LIST(167),
	// Message: $s1 has requested to become friends.
	S1_HAS_REQUESTED_TO_BECOME_FRIENDS(168),
	// Message: Accept friendship 0/1 (1 to accept, 0 to deny)
	ACCEPT_FRIENDSHIP_01_1_TO_ACCEPT_0_TO_DENY(169),
	// Message: The user who requested to become friends is not found in the game.
	THE_USER_WHO_REQUESTED_TO_BECOME_FRIENDS_IS_NOT_FOUND_IN_THE_GAME(170),
	// Message: $s1 is not on your friend list.
	S1_IS_NOT_ON_YOUR_FRIEND_LIST(171),
	// Message: You lack the funds needed to pay for this transaction.
	YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION(172),
	// Message: You lack the funds needed to pay for this transaction.
	YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION_(173),
	// Message: That person's inventory is full.
	THAT_PERSONS_INVENTORY_IS_FULL(174),
	// Message: That skill has been de-activated as HP was fully recovered.
	THAT_SKILL_HAS_BEEN_DEACTIVATED_AS_HP_WAS_FULLY_RECOVERED(175),
	// Message: That person is in message refusal mode.
	THAT_PERSON_IS_IN_MESSAGE_REFUSAL_MODE(176),
	// Message: Message refusal mode.
	MESSAGE_REFUSAL_MODE(177),
	// Message: Message acceptance mode.
	MESSAGE_ACCEPTANCE_MODE(178),
	// Message: You cannot discard those items here.
	YOU_CANNOT_DISCARD_THOSE_ITEMS_HERE(179),
	// Message: You have $s1 day(s) left until deletion.  Do you wish to cancel this action?
	YOU_HAVE_S1_DAYS_LEFT_UNTIL_DELETION__DO_YOU_WISH_TO_CANCEL_THIS_ACTION(180),
	// Message: Cannot see target.
	CANNOT_SEE_TARGET(181),
	// Message: Do you want to quit the current quest?
	DO_YOU_WANT_TO_QUIT_THE_CURRENT_QUEST(182),
	// Message: There are too many users on the server. Please try again later.
	THERE_ARE_TOO_MANY_USERS_ON_THE_SERVER_PLEASE_TRY_AGAIN_LATER(183),
	// Message: Please try again later.
	PLEASE_TRY_AGAIN_LATER(184),
	// Message: You must first select a user to invite to your party.
	YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE_TO_YOUR_PARTY(185),
	// Message: You must first select a user to invite to your clan.
	YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE_TO_YOUR_CLAN(186),
	// Message: Select user to expel.
	SELECT_USER_TO_EXPEL(187),
	// Message: Please create your clan name.
	PLEASE_CREATE_YOUR_CLAN_NAME(188),
	// Message: Your clan has been created.
	YOUR_CLAN_HAS_BEEN_CREATED(189),
	// Message: You have failed to create a clan.
	YOU_HAVE_FAILED_TO_CREATE_A_CLAN(190),
	// Message: Clan member $s1 has been expelled.
	CLAN_MEMBER_S1_HAS_BEEN_EXPELLED(191),
	// Message: You have failed to expel $s1 from the clan.
	YOU_HAVE_FAILED_TO_EXPEL_S1_FROM_THE_CLAN(192),
	// Message: Clan has dispersed.
	CLAN_HAS_DISPERSED(193),
	// Message: You have failed to disperse the clan.
	YOU_HAVE_FAILED_TO_DISPERSE_THE_CLAN(194),
	// Message: Entered the clan.
	ENTERED_THE_CLAN(195),
	// Message: $s1 declined your clan invitation.
	S1_DECLINED_YOUR_CLAN_INVITATION(196),
	// Message: You have withdrawn from the clan.
	YOU_HAVE_WITHDRAWN_FROM_THE_CLAN(197),
	// Message: You have failed to withdraw from the $s1 clan.
	YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_S1_CLAN(198),
	// Message: You have recently been dismissed from a clan.  You are not allowed to join another clan for 24-hours.
	YOU_HAVE_RECENTLY_BEEN_DISMISSED_FROM_A_CLAN__YOU_ARE_NOT_ALLOWED_TO_JOIN_ANOTHER_CLAN_FOR_24HOURS(199),
	// Message: You have withdrawn from the party.
	YOU_HAVE_WITHDRAWN_FROM_THE_PARTY(200),
	// Message: $s1 was expelled from the party.
	S1_WAS_EXPELLED_FROM_THE_PARTY(201),
	// Message: You have been expelled from the party.
	YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY(202),
	// Message: The party has dispersed.
	THE_PARTY_HAS_DISPERSED(203),
	// Message: Incorrect name. Please try again.
	INCORRECT_NAME_PLEASE_TRY_AGAIN(204),
	// Message: Incorrect character name.  Please try again.
	INCORRECT_CHARACTER_NAME__PLEASE_TRY_AGAIN(205),
	// Message: Please enter the name of the clan you wish to declare war on.
	PLEASE_ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_DECLARE_WAR_ON(206),
	// Message: $s2 of the clan $s1 requests declaration of war. Do you accept?
	S2_OF_THE_CLAN_S1_REQUESTS_DECLARATION_OF_WAR_DO_YOU_ACCEPT(207),
	// Message: Please include file type when entering file path.
	PLEASE_INCLUDE_FILE_TYPE_WHEN_ENTERING_FILE_PATH(208),
	// Message: The size of the image file is inappropriate.  Please adjust to 16*12.
	THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE__PLEASE_ADJUST_TO_1612(209),
	// Message: Cannot find file. Please enter precise path.
	CANNOT_FIND_FILE_PLEASE_ENTER_PRECISE_PATH(210),
	// Message: You may only register a 16 x 12 pixel, 256-color BMP.
	YOU_MAY_ONLY_REGISTER_A_16_X_12_PIXEL_256COLOR_BMP(211),
	// Message: You are not a clan member and cannot perform this action.
	YOU_ARE_NOT_A_CLAN_MEMBER_AND_CANNOT_PERFORM_THIS_ACTION(212),
	// Message: Not working. Please try again later.
	NOT_WORKING_PLEASE_TRY_AGAIN_LATER(213),
	// Message: Your title has been changed.
	YOUR_TITLE_HAS_BEEN_CHANGED(214),
	// Message: War with the $s1 clan has begun.
	WAR_WITH_THE_S1_CLAN_HAS_BEGUN(215),
	// Message: War with the $s1 clan has ended.
	WAR_WITH_THE_S1_CLAN_HAS_ENDED(216),
	// Message: You have won the war over the $s1 clan!
	YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN(217),
	// Message: You have surrendered to the $s1 clan.
	YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN(218),
	// Message: Your clan leader has died. You have been defeated by the $s1 Clan.
	YOUR_CLAN_LEADER_HAS_DIED_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN(219),
	// Message: You have $s1 minutes left until the clan war ends.
	YOU_HAVE_S1_MINUTES_LEFT_UNTIL_THE_CLAN_WAR_ENDS(220),
	// Message: The time limit for the clan war is up. War with the $s1 clan is over.
	THE_TIME_LIMIT_FOR_THE_CLAN_WAR_IS_UP_WAR_WITH_THE_S1_CLAN_IS_OVER(221),
	// Message: $s1 has joined the clan.
	S1_HAS_JOINED_THE_CLAN(222),
	// Message: $s1 has withdrawn from the clan.
	S1_HAS_WITHDRAWN_FROM_THE_CLAN(223),
	// Message: $s1 did not respond: Invitation to the clan has been cancelled.
	S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED(224),
	// Message: You didn't respond to $s1's invitation: joining has been cancelled.
	YOU_DIDNT_RESPOND_TO_S1S_INVITATION_JOINING_HAS_BEEN_CANCELLED(225),
	// Message: The $s1 clan did not respond: war proclamation has been refused.
	THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED(226),
	// Message: Clan war has been refused because you did not respond to $s1 clan's war proclamation.
	CLAN_WAR_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLANS_WAR_PROCLAMATION(227),
	// Message: Request to end war has been denied.
	REQUEST_TO_END_WAR_HAS_BEEN_DENIED(228),
	// Message: You do not meet the criteria ir order to create a clan.
	YOU_DO_NOT_MEET_THE_CRITERIA_IR_ORDER_TO_CREATE_A_CLAN(229),
	// Message: You must wait 10 days before creating a new clan.
	YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN(230),
	// Message: After a clan member is dismissed from a clan, the clan must wait at least a day before accepting a new member.
	AFTER_A_CLAN_MEMBER_IS_DISMISSED_FROM_A_CLAN_THE_CLAN_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_ACCEPTING_A_NEW_MEMBER(231),
	// Message: After leaving or having been dismissed from a clan, you must wait at least a day before joining another clan.
	AFTER_LEAVING_OR_HAVING_BEEN_DISMISSED_FROM_A_CLAN_YOU_MUST_WAIT_AT_LEAST_A_DAY_BEFORE_JOINING_ANOTHER_CLAN(232),
	// Message: The Academy/Royal Guard/Order of Knights is full and cannot accept new members at this time.
	THE_ACADEMYROYAL_GUARDORDER_OF_KNIGHTS_IS_FULL_AND_CANNOT_ACCEPT_NEW_MEMBERS_AT_THIS_TIME(233),
	// Message: The target must be a clan member.
	THE_TARGET_MUST_BE_A_CLAN_MEMBER(234),
	// Message: You are not authorized to bestow these rights.
	YOU_ARE_NOT_AUTHORIZED_TO_BESTOW_THESE_RIGHTS(235),
	// Message: Only the clan leader is enabled.
	ONLY_THE_CLAN_LEADER_IS_ENABLED(236),
	// Message: The clan leader could not be found.
	THE_CLAN_LEADER_COULD_NOT_BE_FOUND(237),
	// Message: Not joined in any clan.
	NOT_JOINED_IN_ANY_CLAN(238),
	// Message: The clan leader cannot withdraw.
	THE_CLAN_LEADER_CANNOT_WITHDRAW(239),
	// Message: Currently involved in clan war.
	CURRENTLY_INVOLVED_IN_CLAN_WAR(240),
	// Message: Leader of the $s1 Clan is not logged in.
	LEADER_OF_THE_S1_CLAN_IS_NOT_LOGGED_IN(241),
	// Message: Select target.
	SELECT_TARGET(242),
	// Message: You cannot declare war on an allied clan.
	YOU_CANNOT_DECLARE_WAR_ON_AN_ALLIED_CLAN(243),
	// Message: You are not allowed to issue this challenge.
	YOU_ARE_NOT_ALLOWED_TO_ISSUE_THIS_CHALLENGE(244),
	// Message: 5 days has not passed since you were refused war. Do you wish to continue?
	_5_DAYS_HAS_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR_DO_YOU_WISH_TO_CONTINUE(245),
	// Message: That clan is currently at war.
	THAT_CLAN_IS_CURRENTLY_AT_WAR(246),
	// Message: You have already been at war with the $s1 clan: 5 days must pass before you can challenge this clan again.
	YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_CHALLENGE_THIS_CLAN_AGAIN(247),
	// Message: You cannot proclaim war: the $s1 clan does not have enough members.
	YOU_CANNOT_PROCLAIM_WAR_THE_S1_CLAN_DOES_NOT_HAVE_ENOUGH_MEMBERS(248),
	// Message: Do you wish to surrender to the $s1 clan?
	DO_YOU_WISH_TO_SURRENDER_TO_THE_S1_CLAN(249),
	// Message: You have personally surrendered to the $s1 clan.  You are no longer participating in this clan war.
	YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN__YOU_ARE_NO_LONGER_PARTICIPATING_IN_THIS_CLAN_WAR(250),
	// Message: You cannot proclaim war: you are at war with another clan.
	YOU_CANNOT_PROCLAIM_WAR_YOU_ARE_AT_WAR_WITH_ANOTHER_CLAN(251),
	// Message: Enter the name of clan to surrender to.
	ENTER_THE_NAME_OF_CLAN_TO_SURRENDER_TO(252),
	// Message: Enter the name of the clan you wish to end the war with.
	ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_END_THE_WAR_WITH(253),
	// Message: A clan leader cannot personally surrender.
	A_CLAN_LEADER_CANNOT_PERSONALLY_SURRENDER(254),
	// Message: The $s1 Clan has requested to end war. Do you agree?
	THE_S1_CLAN_HAS_REQUESTED_TO_END_WAR_DO_YOU_AGREE(255),
	// Message: Enter Title
	ENTER_TITLE(256),
	// Message: Do you offer the $s1 clan a proposal to end the war?
	DO_YOU_OFFER_THE_S1_CLAN_A_PROPOSAL_TO_END_THE_WAR(257),
	// Message: You are not involved in a clan war.
	YOU_ARE_NOT_INVOLVED_IN_A_CLAN_WAR(258),
	// Message: Select clan members from list.
	SELECT_CLAN_MEMBERS_FROM_LIST(259),
	// Message: Fame level has decreased: 5 days have not passed since you were refused war.
	FAME_LEVEL_HAS_DECREASED_5_DAYS_HAVE_NOT_PASSED_SINCE_YOU_WERE_REFUSED_WAR(260),
	// Message: Clan name is incorrect.
	CLAN_NAME_IS_INCORRECT(261),
	// Message: Clan name's length is incorrect.
	CLAN_NAMES_LENGTH_IS_INCORRECT(262),
	// Message: You have already requested the dissolution of your clan.
	YOU_HAVE_ALREADY_REQUESTED_THE_DISSOLUTION_OF_YOUR_CLAN(263),
	// Message: You cannot dissolve a clan while engaged in a war.
	YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR(264),
	// Message: You cannot dissolve a clan during a siege or while protecting a castle.
	YOU_CANNOT_DISSOLVE_A_CLAN_DURING_A_SIEGE_OR_WHILE_PROTECTING_A_CASTLE(265),
	// Message: You cannot dissolve a clan while owning a clan hall or castle.
	YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_OWNING_A_CLAN_HALL_OR_CASTLE(266),
	// Message: There are no requests to disperse.
	THERE_ARE_NO_REQUESTS_TO_DISPERSE(267),
	// Message: That player already belongs to another clan.
	THAT_PLAYER_ALREADY_BELONGS_TO_ANOTHER_CLAN(268),
	// Message: You cannot dismiss yourself.
	YOU_CANNOT_DISMISS_YOURSELF(269),
	// Message: You have already surrendered.
	YOU_HAVE_ALREADY_SURRENDERED(270),
	// Message: A player can only be granted a title if the clan is level 3 or above.
	A_PLAYER_CAN_ONLY_BE_GRANTED_A_TITLE_IF_THE_CLAN_IS_LEVEL_3_OR_ABOVE(271),
	// Message: A clan crest can only be registered when the clan's skill level is 3 or above.
	A_CLAN_CREST_CAN_ONLY_BE_REGISTERED_WHEN_THE_CLANS_SKILL_LEVEL_IS_3_OR_ABOVE(272),
	// Message: A clan war can only be declared when a clan's skill level is 3 or above.
	A_CLAN_WAR_CAN_ONLY_BE_DECLARED_WHEN_A_CLANS_SKILL_LEVEL_IS_3_OR_ABOVE(273),
	// Message: Your clan's skill level has increased.
	YOUR_CLANS_SKILL_LEVEL_HAS_INCREASED(274),
	// Message: Clan has failed to increase skill level.
	CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL(275),
	// Message: You do not have the necessary materials or prerequisites to learn this skill.
	YOU_DO_NOT_HAVE_THE_NECESSARY_MATERIALS_OR_PREREQUISITES_TO_LEARN_THIS_SKILL(276),
	// Message: You have earned $s1.
	YOU_HAVE_EARNED_S1_(277),
	// Message: You do not have enough SP to learn this skill.
	YOU_DO_NOT_HAVE_ENOUGH_SP_TO_LEARN_THIS_SKILL(278),
	// Message: You do not have enough adena.
	YOU_DO_NOT_HAVE_ENOUGH_ADENA(279),
	// Message: You do not have any items to sell.
	YOU_DO_NOT_HAVE_ANY_ITEMS_TO_SELL(280),
	// Message: You do not have enough adena to pay the fee.
	YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_PAY_THE_FEE(281),
	// Message: You have not deposited any items in your warehouse.
	YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE(282),
	// Message: You have entered a combat zone.
	YOU_HAVE_ENTERED_A_COMBAT_ZONE(283),
	// Message: You have left a combat zone.
	YOU_HAVE_LEFT_A_COMBAT_ZONE(284),
	// Message: Clan $s1 has succeeded in engraving the ruler!
	CLAN_S1_HAS_SUCCEEDED_IN_ENGRAVING_THE_RULER(285),
	// Message: Your base is being attacked.
	YOUR_BASE_IS_BEING_ATTACKED(286),
	// Message: The opposing clan has started to engrave the monument!
	THE_OPPOSING_CLAN_HAS_STARTED_TO_ENGRAVE_THE_MONUMENT(287),
	// Message: The castle gate has been broken down.
	THE_CASTLE_GATE_HAS_BEEN_BROKEN_DOWN(288),
	// Message: You cannot build another headquarters since one already exists.
	YOU_CANNOT_BUILD_ANOTHER_HEADQUARTERS_SINCE_ONE_ALREADY_EXISTS(289),
	// Message: You cannot set up a base here.
	YOU_CANNOT_SET_UP_A_BASE_HERE(290),
	// Message: Clan $s1 is victorious over $s2's castle siege!
	CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE(291),
	// Message: $s1 has announced the castle siege time.
	S1_HAS_ANNOUNCED_THE_CASTLE_SIEGE_TIME(292),
	// Message: The registration term for $s1 has ended.
	THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED(293),
	// Message: Because your clan is not currently on the offensive in a Clan Hall siege war, it cannot summon its base camp.
	BECAUSE_YOUR_CLAN_IS_NOT_CURRENTLY_ON_THE_OFFENSIVE_IN_A_CLAN_HALL_SIEGE_WAR_IT_CANNOT_SUMMON_ITS_BASE_CAMP(294),
	// Message: $s1's siege was canceled because there were no clans that participated.
	S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED(295),
	// Message: You received $s1 damage from taking a high fall.
	YOU_RECEIVED_S1_DAMAGE_FROM_TAKING_A_HIGH_FALL(296),
	// Message: You have taken $s1 damage because you were unable to breathe.
	YOU_HAVE_TAKEN_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE(297),
	// Message: You have dropped $s1.
	YOU_HAVE_DROPPED_S1(298),
	// Message: $s1 has obtained $s3 $s2.
	S1_HAS_OBTAINED_S3_S2(299),
	// Message: $s1 has obtained $s2.
	S1_HAS_OBTAINED_S2(300),
	// Message: $s2 $s1 has disappeared.
	S2_S1_HAS_DISAPPEARED(301),
	// Message: $s1 has disappeared.
	S1_HAS_DISAPPEARED(302),
	// Message: Select item to enchant.
	SELECT_ITEM_TO_ENCHANT(303),
	// Message: Clan member $s1 has logged into game.
	CLAN_MEMBER_S1_HAS_LOGGED_INTO_GAME(304),
	// Message: The player declined to join your party.
	THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY(305),
	// Message: You have failed to delete the character.
	YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER(306),
	// Message: You cannot trade with a warehouse keeper.
	YOU_CANNOT_TRADE_WITH_A_WAREHOUSE_KEEPER(307),
	// Message: The player declined your clan invitation.
	THE_PLAYER_DECLINED_YOUR_CLAN_INVITATION(308),
	// Message: You have succeeded in expelling the clan member.
	YOU_HAVE_SUCCEEDED_IN_EXPELLING_THE_CLAN_MEMBER(309),
	// Message: You have failed to expel the clan member.
	YOU_HAVE_FAILED_TO_EXPEL_THE_CLAN_MEMBER(310),
	// Message: The clan war declaration has been accepted.
	THE_CLAN_WAR_DECLARATION_HAS_BEEN_ACCEPTED(311),
	// Message: The clan war declaration has been refused.
	THE_CLAN_WAR_DECLARATION_HAS_BEEN_REFUSED(312),
	// Message: The cease war request has been accepted.
	THE_CEASE_WAR_REQUEST_HAS_BEEN_ACCEPTED(313),
	// Message: You have failed to surrender.
	YOU_HAVE_FAILED_TO_SURRENDER(314),
	// Message: You have failed to personally surrender.
	YOU_HAVE_FAILED_TO_PERSONALLY_SURRENDER(315),
	// Message: You have failed to withdraw from the party.
	YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_PARTY(316),
	// Message: You have failed to expel the party member.
	YOU_HAVE_FAILED_TO_EXPEL_THE_PARTY_MEMBER(317),
	// Message: You have failed to disperse the party.
	YOU_HAVE_FAILED_TO_DISPERSE_THE_PARTY(318),
	// Message: This door cannot be unlocked.
	THIS_DOOR_CANNOT_BE_UNLOCKED(319),
	// Message: You have failed to unlock the door.
	YOU_HAVE_FAILED_TO_UNLOCK_THE_DOOR(320),
	// Message: It is not locked.
	IT_IS_NOT_LOCKED(321),
	// Message: Please decide on the sales price.
	PLEASE_DECIDE_ON_THE_SALES_PRICE(322),
	// Message: Your force has increased to $s1 level.
	YOUR_FORCE_HAS_INCREASED_TO_S1_LEVEL(323),
	// Message: Your force has reached maximum capacity.
	YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY(324),
	// Message: The corpse has already disappeared.
	THE_CORPSE_HAS_ALREADY_DISAPPEARED(325),
	// Message: Select target from list.
	SELECT_TARGET_FROM_LIST(326),
	// Message: You cannot exceed 80 characters.
	YOU_CANNOT_EXCEED_80_CHARACTERS(327),
	// Message: Please input title using less than 128 characters.
	PLEASE_INPUT_TITLE_USING_LESS_THAN_128_CHARACTERS(328),
	// Message: Please input contents using less than 3000 characters.
	PLEASE_INPUT_CONTENTS_USING_LESS_THAN_3000_CHARACTERS(329),
	// Message: A one-line response may not exceed 128 characters.
	A_ONELINE_RESPONSE_MAY_NOT_EXCEED_128_CHARACTERS(330),
	// Message: You have acquired $s1 SP.
	YOU_HAVE_ACQUIRED_S1_SP(331),
	// Message: Do you want to be restored?
	DO_YOU_WANT_TO_BE_RESTORED(332),
	// Message: You have received $s1 damage by Core's barrier.
	YOU_HAVE_RECEIVED_S1_DAMAGE_BY_CORES_BARRIER(333),
	// Message: Please enter your private store display message.
	PLEASE_ENTER_YOUR_PRIVATE_STORE_DISPLAY_MESSAGE(334),
	// Message: $s1 has been aborted.
	S1_HAS_BEEN_ABORTED(335),
	// Message: You are attempting to crystalize $s1.  Do you wish to continue?
	YOU_ARE_ATTEMPTING_TO_CRYSTALIZE_S1__DO_YOU_WISH_TO_CONTINUE(336),
	// Message: The soulshot you are attempting to use does not match the grade of your equipped weapon.
	THE_SOULSHOT_YOU_ARE_ATTEMPTING_TO_USE_DOES_NOT_MATCH_THE_GRADE_OF_YOUR_EQUIPPED_WEAPON(337),
	// Message: You do not have enough soulshots for that.
	YOU_DO_NOT_HAVE_ENOUGH_SOULSHOTS_FOR_THAT(338),
	// Message: Cannot use soulshots.
	CANNOT_USE_SOULSHOTS(339),
	// Message: Your private store is now open for business.
	YOUR_PRIVATE_STORE_IS_NOW_OPEN_FOR_BUSINESS(340),
	// Message: You do not have enough materials to perform that action.
	YOU_DO_NOT_HAVE_ENOUGH_MATERIALS_TO_PERFORM_THAT_ACTION(341),
	// Message: Power of the spirits enabled.
	POWER_OF_THE_SPIRITS_ENABLED(342),
	// Message: Sweeper failed, target not spoiled.
	SWEEPER_FAILED_TARGET_NOT_SPOILED(343),
	// Message: Power of the spirits disabled.
	POWER_OF_THE_SPIRITS_DISABLED(344),
	// Message: Chat enabled.
	CHAT_ENABLED(345),
	// Message: Chat disabled.
	CHAT_DISABLED(346),
	// Message: Incorrect item count.
	INCORRECT_ITEM_COUNT(347),
	// Message: Incorrect item price.
	INCORRECT_ITEM_PRICE(348),
	// Message: Private store already closed.
	PRIVATE_STORE_ALREADY_CLOSED(349),
	// Message: Item out of stock.
	ITEM_OUT_OF_STOCK(350),
	// Message: Incorrect item count.
	INCORRECT_ITEM_COUNT_(351),
	// Message: Incorrect item.
	INCORRECT_ITEM(352),
	// Message: Cannot purchase.
	CANNOT_PURCHASE(353),
	// Message: Cancel enchant.
	CANCEL_ENCHANT(354),
	// Message: Inappropriate enchant conditions.
	INAPPROPRIATE_ENCHANT_CONDITIONS(355),
	// Message: Reject resurrection.
	REJECT_RESURRECTION(356),
	// Message: It has already been spoiled.
	IT_HAS_ALREADY_BEEN_SPOILED(357),
	// Message: $s1 hour(s) until castle siege conclusion.
	S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION(358),
	// Message: $s1 minute(s) until castle siege conclusion.
	S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION(359),
	// Message: Castle siege $s1 second(s) left!
	CASTLE_SIEGE_S1_SECONDS_LEFT(360),
	// Message: Over-hit!
	OVERHIT(361),
	// Message: You have acquired $s1 bonus experience from a successful over-hit.
	YOU_HAVE_ACQUIRED_S1_BONUS_EXPERIENCE_FROM_A_SUCCESSFUL_OVERHIT(362),
	// Message: Chat available time: $s1 minute.
	CHAT_AVAILABLE_TIME_S1_MINUTE(363),
	// Message: Enter user's name to search.
	ENTER_USERS_NAME_TO_SEARCH(364),
	// Message: Are you sure?
	ARE_YOU_SURE(365),
	// Message: Please select your hair color.
	PLEASE_SELECT_YOUR_HAIR_COLOR(366),
	// Message: You cannot remove that clan character at this time.
	YOU_CANNOT_REMOVE_THAT_CLAN_CHARACTER_AT_THIS_TIME(367),
	// Message: Equipped +$s1 $s2.
	EQUIPPED_S1_S2(368),
	// Message: You have obtained a +$s1 $s2.
	YOU_HAVE_OBTAINED_A_S1_S2(369),
	// Message: Failed to pick up $s1.
	FAILED_TO_PICK_UP_S1(370),
	// Message: Acquired +$s1 $s2.
	ACQUIRED_S1_S2(371),
	// Message: Failed to earn $s1.
	FAILED_TO_EARN_S1(372),
	// Message: You are trying to destroy +$s1 $s2.  Do you wish to continue?
	YOU_ARE_TRYING_TO_DESTROY_S1_S2__DO_YOU_WISH_TO_CONTINUE(373),
	// Message: You are attempting to crystalize +$s1 $s2.  Do you wish to continue?
	YOU_ARE_ATTEMPTING_TO_CRYSTALIZE_S1_S2__DO_YOU_WISH_TO_CONTINUE(374),
	// Message: You have dropped +$s1 $s2.
	YOU_HAVE_DROPPED_S1_S2(375),
	// Message: $s1 has obtained +$s2$s3.
	S1_HAS_OBTAINED_S2S3(376),
	// Message: $S1 $S2 disappeared.
	S1_S2_DISAPPEARED(377),
	// Message: $s1 purchased $s2.
	S1_PURCHASED_S2(378),
	// Message: $s1 purchased +$s2 $s3.
	S1_PURCHASED_S2_S3(379),
	// Message: $s1 purchased $s3 $s2(s).
	S1_PURCHASED_S3_S2S(380),
	// Message: The game client encountered an error and was unable to connect to the petition server.
	THE_GAME_CLIENT_ENCOUNTERED_AN_ERROR_AND_WAS_UNABLE_TO_CONNECT_TO_THE_PETITION_SERVER(381),
	// Message: Currently there are no users that have checked out a GM ID.
	CURRENTLY_THERE_ARE_NO_USERS_THAT_HAVE_CHECKED_OUT_A_GM_ID(382),
	// Message: Request confirmed to end consultation at petition server.
	REQUEST_CONFIRMED_TO_END_CONSULTATION_AT_PETITION_SERVER(383),
	// Message: The client is not logged onto the game server.
	THE_CLIENT_IS_NOT_LOGGED_ONTO_THE_GAME_SERVER(384),
	// Message: Request confirmed to begin consultation at petition server.
	REQUEST_CONFIRMED_TO_BEGIN_CONSULTATION_AT_PETITION_SERVER(385),
	// Message: The body of your petition must be more than five characters in length.
	THE_BODY_OF_YOUR_PETITION_MUST_BE_MORE_THAN_FIVE_CHARACTERS_IN_LENGTH(386),
	// Message: This ends the GM petition consultation.     \\n Please take a moment to provide feedback about this service.
	THIS_ENDS_THE_GM_PETITION_CONSULTATION_____N_PLEASE_TAKE_A_MOMENT_TO_PROVIDE_FEEDBACK_ABOUT_THIS_SERVICE(387),
	// Message: Not under petition consultation.
	NOT_UNDER_PETITION_CONSULTATION(388),
	// Message: Your petition application has been accepted. \\n - Receipt No. is $s1.
	YOUR_PETITION_APPLICATION_HAS_BEEN_ACCEPTED_N__RECEIPT_NO_IS_S1(389),
	// Message: You may only submit one petition (active) at a time.
	YOU_MAY_ONLY_SUBMIT_ONE_PETITION_ACTIVE_AT_A_TIME(390),
	// Message: Receipt No. $s1, petition cancelled.
	RECEIPT_NO_S1_PETITION_CANCELLED(391),
	// Message: Under petition advice.
	UNDER_PETITION_ADVICE(392),
	// Message: Failed to cancel petition. Please try again later.
	FAILED_TO_CANCEL_PETITION_PLEASE_TRY_AGAIN_LATER(393),
	// Message: Petition consultation with $s1, under way.
	PETITION_CONSULTATION_WITH_S1_UNDER_WAY(394),
	// Message: Ending petition consultation with $s1.
	ENDING_PETITION_CONSULTATION_WITH_S1(395),
	// Message: Please login after changing your temporary password.
	PLEASE_LOGIN_AFTER_CHANGING_YOUR_TEMPORARY_PASSWORD(396),
	// Message: Not a paid account.
	NOT_A_PAID_ACCOUNT(397),
	// Message: There is no time left on this account.
	THERE_IS_NO_TIME_LEFT_ON_THIS_ACCOUNT(398),
	// Message: System error.
	SYSTEM_ERROR(399),
	// Message: You are attempting to drop $s1.  Do you wish to continue?
	YOU_ARE_ATTEMPTING_TO_DROP_S1__DO_YOU_WISH_TO_CONTINUE(400),
	// Message: You currently have too many quests in progress.
	YOU_CURRENTLY_HAVE_TOO_MANY_QUESTS_IN_PROGRESS(401),
	// Message: You do not possess the correct ticket to board the boat.
	YOU_DO_NOT_POSSESS_THE_CORRECT_TICKET_TO_BOARD_THE_BOAT(402),
	// Message: You have exceeded your out-of-pocket adena limit.
	YOU_HAVE_EXCEEDED_YOUR_OUTOFPOCKET_ADENA_LIMIT(403),
	// Message: Your Create Item level is too low to register this recipe.
	YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE(404),
	// Message: The total price of the product is too high.
	THE_TOTAL_PRICE_OF_THE_PRODUCT_IS_TOO_HIGH(405),
	// Message: Petition application accepted.
	PETITION_APPLICATION_ACCEPTED(406),
	// Message: Petition under process.
	PETITION_UNDER_PROCESS(407),
	// Message: Set Period
	SET_PERIOD(408),
	// Message: Set Time-$s1: $s2: $s3
	SET_TIMES1_S2_S3(409),
	// Message: Registration Period
	REGISTRATION_PERIOD(410),
	// Message: Registration TIme-$s1: $s2: $s3
	REGISTRATION_TIMES1_S2_S3(411),
	// Message: Battle begins in $s1: $s2: $s4
	BATTLE_BEGINS_IN_S1_S2_S4(412),
	// Message: Battle ends in $s1: $s2: $s5
	BATTLE_ENDS_IN_S1_S2_S5(413),
	// Message: Standby
	STANDBY(414),
	// Message: Under Siege
	UNDER_SIEGE(415),
	// Message: This item cannot be exchanged.
	THIS_ITEM_CANNOT_BE_EXCHANGED(416),
	// Message: $s1  has been disarmed.
	S1__HAS_BEEN_DISARMED(417),
	// Message: There is a significant difference between the item's price and its standard price. Please check again.
	THERE_IS_A_SIGNIFICANT_DIFFERENCE_BETWEEN_THE_ITEMS_PRICE_AND_ITS_STANDARD_PRICE_PLEASE_CHECK_AGAIN(418),
	// Message: $s1 minute(s) of usage time left.
	S1_MINUTES_OF_USAGE_TIME_LEFT(419),
	// Message: Time expired.
	TIME_EXPIRED(420),
	// Message: Another person has logged in with the same account.
	ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT(421),
	// Message: You have exceeded the weight limit.
	YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT(422),
	// Message: You have cancelled the enchanting process.
	YOU_HAVE_CANCELLED_THE_ENCHANTING_PROCESS(423),
	// Message: Does not fit strengthening conditions of the scroll.
	DOES_NOT_FIT_STRENGTHENING_CONDITIONS_OF_THE_SCROLL(424),
	// Message: Your Create Item level is too low to register this recipe.
	YOUR_CREATE_ITEM_LEVEL_IS_TOO_LOW_TO_REGISTER_THIS_RECIPE_(425),
	// Message: Your account has been reported for intentionally not paying the cyber café fees.
	YOUR_ACCOUNT_HAS_BEEN_REPORTED_FOR_INTENTIONALLY_NOT_PAYING_THE_CYBER_CAF_FEES(426),
	// Message: Please contact us.
	PLEASE_CONTACT_US(427),
	// Message: In accordance with company policy, this account has been suspended due to suspicion of illegal use and/or misappropriation of another player's data.  Details of the incident(s) in question have been sent to the email address on file with the company.  If you are not directly involved with the reported conduct and wish to submit an appeal, visit the Lineage II official support home page (http://support.plaync.com) and go to the "Ask a Question," section to submit a ticket.
	IN_ACCORDANCE_WITH_COMPANY_POLICY_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_DUE_TO_SUSPICION_OF_ILLEGAL_USE_ANDOR_MISAPPROPRIATION_OF_ANOTHER_PLAYERS_DATA__DETAILS_OF_THE_INCIDENTS_IN_QUESTION_HAVE_BEEN_SENT_TO_THE_EMAIL_ADDRESS_ON_FILE_WITH_THE_COMPANY__IF_YOU_ARE_NOT_DIRECTLY_INVOLVED_WITH_THE_REPORTED_CONDUCT_AND_WISH_TO_SUBMIT_AN_APPEAL_VISIT_THE_LINEAGE_II_OFFICIAL_SUPPORT_HOME_PAGE_HTTPSUPPORTPLAYNCCOM_AND_GO_TO_THE_ASK_A_QUESTION_SECTION_TO_SUBMIT_A_TICKET(428),
	// Message: 번역불필요
	//(429),
	// Message: 번역불필요  (Doesn't need to translate.)
	__DOESNT_NEED_TO_TRANSLATE(430),
	// Message: In accordance with the company's operational, EULA, RoC, and/or User Agreement, this account has been terminated due to a violation by the account holder.  When a  user violates the terms of the User Agreement, the company can impose a restriction on the applicable user's account {Section 14 of the End User License Agreement.} For additional details please contact the company's Lineage II Customer Service Center via the support site at http://support.plaync.com.
	IN_ACCORDANCE_WITH_THE_COMPANYS_OPERATIONAL_EULA_ROC_ANDOR_USER_AGREEMENT_THIS_ACCOUNT_HAS_BEEN_TERMINATED_DUE_TO_A_VIOLATION_BY_THE_ACCOUNT_HOLDER__WHEN_A__USER_VIOLATES_THE_TERMS_OF_THE_USER_AGREEMENT_THE_COMPANY_CAN_IMPOSE_A_RESTRICTION_ON_THE_APPLICABLE_USERS_ACCOUNT_SECTION_14_OF_THE_END_USER_LICENSE_AGREEMENT_FOR_ADDITIONAL_DETAILS_PLEASE_CONTACT_THE_COMPANYS_LINEAGE_II_CUSTOMER_SERVICE_CENTER_VIA_THE_SUPPORT_SITE_AT_HTTPSUPPORTPLAYNCCOM(431),
	// Message: 번역불필요
	//(432),
	// Message: 번역불필요
	//(433),
	// Message: 번역불필요
	//(434),
	// Message: 번역불필요
	//(435),
	// Message: 번역불필요
	//(436),
	// Message: 번역불필요
	//(437),
	// Message: 번역불필요
	//(438),
	// Message: In accordance with the company's User Agreement and Operational Policy this account has been suspended at the account holder's request. If you have any questions regarding your account please contact support at http://support.plaync.com
	IN_ACCORDANCE_WITH_THE_COMPANYS_USER_AGREEMENT_AND_OPERATIONAL_POLICY_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_AT_THE_ACCOUNT_HOLDERS_REQUEST_IF_YOU_HAVE_ANY_QUESTIONS_REGARDING_YOUR_ACCOUNT_PLEASE_CONTACT_SUPPORT_AT_HTTPSUPPORTPLAYNCCOM(439),
	// Message: 번역불필요
	//(440),
	// Message: Per our company's User Agreement, the use of this account has been suspended. If you have any questions regarding your account please contact support at http://support.plaync.com.
	PER_OUR_COMPANYS_USER_AGREEMENT_THE_USE_OF_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_IF_YOU_HAVE_ANY_QUESTIONS_REGARDING_YOUR_ACCOUNT_PLEASE_CONTACT_SUPPORT_AT_HTTPSUPPORTPLAYNCCOM(441),
	// Message: 번역불필요
	//(442),
	// Message: The identity of the owner of this account has not been verified. Therefore, the Lineage II service for this account has been suspended. To verify your identity please contact support at http://support.plaync.com
	THE_IDENTITY_OF_THE_OWNER_OF_THIS_ACCOUNT_HAS_NOT_BEEN_VERIFIED_THEREFORE_THE_LINEAGE_II_SERVICE_FOR_THIS_ACCOUNT_HAS_BEEN_SUSPENDED_TO_VERIFY_YOUR_IDENTITY_PLEASE_CONTACT_SUPPORT_AT_HTTPSUPPORTPLAYNCCOM(443),
	// Message: Since we have received a withdrawal request from the holder of this account access to all applicable accounts has been automatically suspended.
	SINCE_WE_HAVE_RECEIVED_A_WITHDRAWAL_REQUEST_FROM_THE_HOLDER_OF_THIS_ACCOUNT_ACCESS_TO_ALL_APPLICABLE_ACCOUNTS_HAS_BEEN_AUTOMATICALLY_SUSPENDED(444),
	// Message: (Reference Number Regarding Membership Withdrawal Request: $s1)
	REFERENCE_NUMBER_REGARDING_MEMBERSHIP_WITHDRAWAL_REQUEST_S1(445),
	// Message: For more details, please contact our customer service center at http://support.plaync.com
	FOR_MORE_DETAILS_PLEASE_CONTACT_OUR_CUSTOMER_SERVICE_CENTER_AT_HTTPSUPPORTPLAYNCCOM(446),
	// Message: .
	//(447),
	// Message: System error, please log in again later.
	SYSTEM_ERROR_PLEASE_LOG_IN_AGAIN_LATER(448),
	// Message: Password does not match this account.
	PASSWORD_DOES_NOT_MATCH_THIS_ACCOUNT(449),
	// Message: Confirm your account information and log in again later.
	CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_LOG_IN_AGAIN_LATER(450),
	// Message: The password you have entered is incorrect.
	THE_PASSWORD_YOU_HAVE_ENTERED_IS_INCORRECT(451),
	// Message: Please confirm your account information and try logging in again.
	PLEASE_CONFIRM_YOUR_ACCOUNT_INFORMATION_AND_TRY_LOGGING_IN_AGAIN(452),
	// Message: Your account information is incorrect.
	YOUR_ACCOUNT_INFORMATION_IS_INCORRECT(453),
	// Message: For more details, please contact our customer service center at http://support.plaync.com
	FOR_MORE_DETAILS_PLEASE_CONTACT_OUR_CUSTOMER_SERVICE_CENTER_AT_HTTPSUPPORTPLAYNCCOM_(454),
	// Message: This account is already in use.  Access denied.
	THIS_ACCOUNT_IS_ALREADY_IN_USE__ACCESS_DENIED(455),
	// Message: Lineage II game services may be used by individuals 15 years of age or older except for PvP servers, which may only be used by adults 18 years of age and older. (Korea Only)
	LINEAGE_II_GAME_SERVICES_MAY_BE_USED_BY_INDIVIDUALS_15_YEARS_OF_AGE_OR_OLDER_EXCEPT_FOR_PVP_SERVERS_WHICH_MAY_ONLY_BE_USED_BY_ADULTS_18_YEARS_OF_AGE_AND_OLDER_KOREA_ONLY(456),
	// Message: Server under maintenance. Please try again later.
	SERVER_UNDER_MAINTENANCE_PLEASE_TRY_AGAIN_LATER(457),
	// Message: Your usage term has expired.
	YOUR_USAGE_TERM_HAS_EXPIRED(458),
	// Message: Please visit the official Lineage II website at http://www.lineage2.com
	PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_WEBSITE_AT_HTTPWWWLINEAGE2COM(459),
	// Message: to reactivate your account.
	TO_REACTIVATE_YOUR_ACCOUNT(460),
	// Message: Access failed.
	ACCESS_FAILED(461),
	// Message: Please try again later.
	PLEASE_TRY_AGAIN_LATER_(462),
	// Message: .
	//(463),
	// Message: This feature is only available alliance leaders.
	THIS_FEATURE_IS_ONLY_AVAILABLE_ALLIANCE_LEADERS(464),
	// Message: You are not currently allied with any clans.
	YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS(465),
	// Message: You have exceeded the limit.
	YOU_HAVE_EXCEEDED_THE_LIMIT(466),
	// Message: You may not accept any clan within a day after expelling another clan.
	YOU_MAY_NOT_ACCEPT_ANY_CLAN_WITHIN_A_DAY_AFTER_EXPELLING_ANOTHER_CLAN(467),
	// Message: A clan that has withdrawn or been expelled cannot enter into an alliance within one day of withdrawal or expulsion.
	A_CLAN_THAT_HAS_WITHDRAWN_OR_BEEN_EXPELLED_CANNOT_ENTER_INTO_AN_ALLIANCE_WITHIN_ONE_DAY_OF_WITHDRAWAL_OR_EXPULSION(468),
	// Message: You may not ally with a clan you are currently at war with.  That would be diabolical and treacherous.
	YOU_MAY_NOT_ALLY_WITH_A_CLAN_YOU_ARE_CURRENTLY_AT_WAR_WITH__THAT_WOULD_BE_DIABOLICAL_AND_TREACHEROUS(469),
	// Message: Only the clan leader may apply for withdrawal from the alliance.
	ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE(470),
	// Message: Alliance leaders cannot withdraw.
	ALLIANCE_LEADERS_CANNOT_WITHDRAW(471),
	// Message: You cannot expel yourself from the clan.
	YOU_CANNOT_EXPEL_YOURSELF_FROM_THE_CLAN(472),
	// Message: Different alliance.
	DIFFERENT_ALLIANCE(473),
	// Message: That clan does not exist.
	THAT_CLAN_DOES_NOT_EXIST(474),
	// Message: Different alliance.
	DIFFERENT_ALLIANCE_(475),
	// Message: Please adjust the image size to 8x12.
	PLEASE_ADJUST_THE_IMAGE_SIZE_TO_8X12(476),
	// Message: No response. Invitation to join an alliance has been cancelled.
	NO_RESPONSE_INVITATION_TO_JOIN_AN_ALLIANCE_HAS_BEEN_CANCELLED(477),
	// Message: No response. Your entrance to the alliance has been cancelled.
	NO_RESPONSE_YOUR_ENTRANCE_TO_THE_ALLIANCE_HAS_BEEN_CANCELLED(478),
	// Message: $s1  has joined as a friend.
	S1__HAS_JOINED_AS_A_FRIEND(479),
	// Message: Please check your friends list.
	PLEASE_CHECK_YOUR_FRIENDS_LIST(480),
	// Message: $s1  has been deleted from your friends list.
	S1__HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST(481),
	// Message: You cannot add yourself to your own friend list.
	YOU_CANNOT_ADD_YOURSELF_TO_YOUR_OWN_FRIEND_LIST_(482),
	// Message: This function is inaccessible right now.  Please try again later.
	THIS_FUNCTION_IS_INACCESSIBLE_RIGHT_NOW__PLEASE_TRY_AGAIN_LATER(483),
	// Message: This player is already registered in your friends list.
	THIS_PLAYER_IS_ALREADY_REGISTERED_IN_YOUR_FRIENDS_LIST(484),
	// Message: No new friend invitations may be accepted.
	NO_NEW_FRIEND_INVITATIONS_MAY_BE_ACCEPTED(485),
	// Message: The following user is not in your friends list.
	THE_FOLLOWING_USER_IS_NOT_IN_YOUR_FRIENDS_LIST(486),
	// Message: ======<Friends List>======
	FRIENDS_LIST(487),
	// Message: $s1 (Currently: Online)
	S1_CURRENTLY_ONLINE(488),
	// Message: $s1 (Currently: Offline)
	S1_CURRENTLY_OFFLINE(489),
	// Message: ========================
	//(490),
	// Message: =======<Alliance Information>=======
	ALLIANCE_INFORMATION(491),
	// Message: Alliance Name: $s1
	ALLIANCE_NAME_S1(492),
	// Message: Connection: $s1 / Total $s2
	CONNECTION_S1__TOTAL_S2(493),
	// Message: Alliance Leader: $s2 of $s1
	ALLIANCE_LEADER_S2_OF_S1(494),
	// Message: Affiliated clans: Total $s1 clan(s)
	AFFILIATED_CLANS_TOTAL_S1_CLANS(495),
	// Message: =====<Clan Information>=====
	CLAN_INFORMATION(496),
	// Message: Clan Name: $s1
	CLAN_NAME_S1(497),
	// Message: Clan Leader:  $s1
	CLAN_LEADER__S1(498),
	// Message: Clan Level: $s1
	CLAN_LEVEL_S1(499),
	// Message: ------------------------
	//(500),
	// Message: ========================
	//(501),
	// Message: You already belong to another alliance.
	YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE(502),
	// Message: $s1 (Friend) has logged in.
	S1_FRIEND_HAS_LOGGED_IN(503),
	// Message: Only clan leaders may create alliances.
	ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES(504),
	// Message: You cannot create a new alliance within 10 days after dissolution.
	YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_10_DAYS_AFTER_DISSOLUTION(505),
	// Message: Incorrect alliance name.  Please try again.
	INCORRECT_ALLIANCE_NAME__PLEASE_TRY_AGAIN(506),
	// Message: Incorrect length for an alliance name.
	INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME(507),
	// Message: This alliance name already exists.
	THIS_ALLIANCE_NAME_ALREADY_EXISTS(508),
	// Message: Cannot accept. clan ally is registered as an enemy during siege battle.
	CANNOT_ACCEPT_CLAN_ALLY_IS_REGISTERED_AS_AN_ENEMY_DURING_SIEGE_BATTLE(509),
	// Message: You have invited someone to your alliance.
	YOU_HAVE_INVITED_SOMEONE_TO_YOUR_ALLIANCE(510),
	// Message: You must first select a user to invite.
	YOU_MUST_FIRST_SELECT_A_USER_TO_INVITE(511),
	// Message: Do you really wish to withdraw from the alliance?
	DO_YOU_REALLY_WISH_TO_WITHDRAW_FROM_THE_ALLIANCE(512),
	// Message: Enter the name of the clan you wish to expel.
	ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_EXPEL(513),
	// Message: Do you really wish to dissolve the alliance?
	DO_YOU_REALLY_WISH_TO_DISSOLVE_THE_ALLIANCE(514),
	// Message: Enter a file name for the alliance crest.
	ENTER_A_FILE_NAME_FOR_THE_ALLIANCE_CREST(515),
	// Message: $s1 has invited you to be their friend.
	S1_HAS_INVITED_YOU_TO_BE_THEIR_FRIEND(516),
	// Message: You have accepted the alliance.
	YOU_HAVE_ACCEPTED_THE_ALLIANCE(517),
	// Message: You have failed to invite a clan into the alliance.
	YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE(518),
	// Message: You have withdrawn from the alliance.
	YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE(519),
	// Message: You have failed to withdraw from the alliance.
	YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE(520),
	// Message: You have succeeded in expelling a clan.
	YOU_HAVE_SUCCEEDED_IN_EXPELLING_A_CLAN(521),
	// Message: You have failed to expel a clan.
	YOU_HAVE_FAILED_TO_EXPEL_A_CLAN(522),
	// Message: The alliance has been dissolved.
	THE_ALLIANCE_HAS_BEEN_DISSOLVED(523),
	// Message: You have failed to dissolve the alliance.
	YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE(524),
	// Message: You have succeeded in inviting a friend to your friends list.
	YOU_HAVE_SUCCEEDED_IN_INVITING_A_FRIEND_TO_YOUR_FRIENDS_LIST(525),
	// Message: You have failed to add a friend to your friends list.
	YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST(526),
	// Message: $s1 leader, $s2, has requested an alliance.
	S1_LEADER_S2_HAS_REQUESTED_AN_ALLIANCE(527),
	// Message: Unable to find file at target location.
	UNABLE_TO_FIND_FILE_AT_TARGET_LOCATION(528),
	// Message: You may only register an 8 x 12 pixel, 256-color BMP.
	YOU_MAY_ONLY_REGISTER_AN_8_X_12_PIXEL_256COLOR_BMP(529),
	// Message: The Spiritshot does not match the weapon's grade.
	THE_SPIRITSHOT_DOES_NOT_MATCH_THE_WEAPONS_GRADE(530),
	// Message: You do not have enough Spiritshots for that.
	YOU_DO_NOT_HAVE_ENOUGH_SPIRITSHOTS_FOR_THAT(531),
	// Message: You may not use Spiritshots.
	YOU_MAY_NOT_USE_SPIRITSHOTS(532),
	// Message: Power of Mana enabled.
	POWER_OF_MANA_ENABLED(533),
	// Message: Power of Mana disabled.
	POWER_OF_MANA_DISABLED(534),
	// Message: Enter a name for your pet.
	ENTER_A_NAME_FOR_YOUR_PET(535),
	// Message: How much adena do you wish to transfer to your Inventory?
	HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_INVENTORY(536),
	// Message: How much will you transfer?
	HOW_MUCH_WILL_YOU_TRANSFER(537),
	// Message: Your SP has decreased by $s1.
	YOUR_SP_HAS_DECREASED_BY_S1(538),
	// Message: Your Experience has decreased by $s1.
	YOUR_EXPERIENCE_HAS_DECREASED_BY_S1(539),
	// Message: Clan leaders may not be deleted. Dissolve the clan first and try again.
	CLAN_LEADERS_MAY_NOT_BE_DELETED_DISSOLVE_THE_CLAN_FIRST_AND_TRY_AGAIN(540),
	// Message: You may not delete a clan member. Withdraw from the clan first and try again.
	YOU_MAY_NOT_DELETE_A_CLAN_MEMBER_WITHDRAW_FROM_THE_CLAN_FIRST_AND_TRY_AGAIN(541),
	// Message: The NPC server is currently down.  Pets and servitors cannot be summoned at this time.
	THE_NPC_SERVER_IS_CURRENTLY_DOWN__PETS_AND_SERVITORS_CANNOT_BE_SUMMONED_AT_THIS_TIME(542),
	// Message: You already have a pet.
	YOU_ALREADY_HAVE_A_PET(543),
	// Message: Your pet cannot carry this item.
	YOUR_PET_CANNOT_CARRY_THIS_ITEM(544),
	// Message: Your pet cannot carry any more items. Remove some, then try again.
	YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS_REMOVE_SOME_THEN_TRY_AGAIN(545),
	// Message: Unable to place item, your pet is too encumbered.
	UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED(546),
	// Message: Summoning your pet…
	SUMMONING_YOUR_PET(547),
	// Message: Your pet's name can be up to 8 characters in length.
	YOUR_PETS_NAME_CAN_BE_UP_TO_8_CHARACTERS_IN_LENGTH(548),
	// Message: To create an alliance, your clan must be Level 5 or higher.
	TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER(549),
	// Message: You may not create an alliance during the term of dissolution postponement.
	YOU_MAY_NOT_CREATE_AN_ALLIANCE_DURING_THE_TERM_OF_DISSOLUTION_POSTPONEMENT(550),
	// Message: You cannot raise your clan level during the term of dispersion postponement.
	YOU_CANNOT_RAISE_YOUR_CLAN_LEVEL_DURING_THE_TERM_OF_DISPERSION_POSTPONEMENT(551),
	// Message: During the grace period for dissolving a clan, the registration or deletion of a clan's crest is not allowed.
	DURING_THE_GRACE_PERIOD_FOR_DISSOLVING_A_CLAN_THE_REGISTRATION_OR_DELETION_OF_A_CLANS_CREST_IS_NOT_ALLOWED(552),
	// Message: The opposing clan has applied for dispersion.
	THE_OPPOSING_CLAN_HAS_APPLIED_FOR_DISPERSION(553),
	// Message: You cannot disperse the clans in your alliance.
	YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE(554),
	// Message: You cannot move - you are too encumbered.
	YOU_CANNOT_MOVE__YOU_ARE_TOO_ENCUMBERED(555),
	// Message: You cannot move in this state.
	YOU_CANNOT_MOVE_IN_THIS_STATE(556),
	// Message: Your pet has been summoned and may not be destroyed.
	YOUR_PET_HAS_BEEN_SUMMONED_AND_MAY_NOT_BE_DESTROYED(557),
	// Message: Your pet has been summoned and cannot be let go.
	YOUR_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_LET_GO(558),
	// Message: You have purchased $s2 from $s1.
	YOU_HAVE_PURCHASED_S2_FROM_S1(559),
	// Message: You have purchased +$s2 $s3 from $s1.
	YOU_HAVE_PURCHASED_S2_S3_FROM_S1(560),
	// Message: You have purchased $s3 $s2(s) from $s1.
	YOU_HAVE_PURCHASED_S3_S2S_FROM_S1(561),
	// Message: You may not crystallize this item. Your crystallization skill level is too low.
	YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW(562),
	// Message: Failed to disable attack target.
	FAILED_TO_DISABLE_ATTACK_TARGET(563),
	// Message: Failed to change attack target.
	FAILED_TO_CHANGE_ATTACK_TARGET(564),
	// Message: Not enough luck.
	NOT_ENOUGH_LUCK(565),
	// Message: Your confusion spell failed.
	YOUR_CONFUSION_SPELL_FAILED(566),
	// Message: Your fear spell failed.
	YOUR_FEAR_SPELL_FAILED(567),
	// Message: Cubic Summoning failed.
	CUBIC_SUMMONING_FAILED(568),
	// Message: Caution -- this item's price greatly differs from non-player run shops. Do you wish to continue?
	CAUTION__THIS_ITEMS_PRICE_GREATLY_DIFFERS_FROM_NONPLAYER_RUN_SHOPS_DO_YOU_WISH_TO_CONTINUE(569),
	// Message: How many  $s1(s) do you want to purchase?
	HOW_MANY__S1S_DO_YOU_WANT_TO_PURCHASE(570),
	// Message: How many  $s1(s) do you want to purchase?
	HOW_MANY__S1S_DO_YOU_WANT_TO_PURCHASE_(571),
	// Message: Do you wish to join $s1's party? (Item distribution: Finders Keepers)
	DO_YOU_WISH_TO_JOIN_S1S_PARTY_ITEM_DISTRIBUTION_FINDERS_KEEPERS(572),
	// Message: Do you wish to join $s1's party? (Item distribution: Random)
	DO_YOU_WISH_TO_JOIN_S1S_PARTY_ITEM_DISTRIBUTION_RANDOM(573),
	// Message: Pets and Servitors are not available at this time.
	PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME(574),
	// Message: How much adena do you wish to transfer to your pet?
	HOW_MUCH_ADENA_DO_YOU_WISH_TO_TRANSFER_TO_YOUR_PET(575),
	// Message: How much do you wish to transfer?
	HOW_MUCH_DO_YOU_WISH_TO_TRANSFER(576),
	// Message: You cannot summon during a trade or while using the private shops.
	YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS(577),
	// Message: You cannot summon during combat.
	YOU_CANNOT_SUMMON_DURING_COMBAT(578),
	// Message: A pet cannot be sent back during battle.
	A_PET_CANNOT_BE_SENT_BACK_DURING_BATTLE(579),
	// Message: You may not use multiple pets or servitors at the same time.
	YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME(580),
	// Message: There is a space in the name.
	THERE_IS_A_SPACE_IN_THE_NAME(581),
	// Message: Inappropriate character name.
	INAPPROPRIATE_CHARACTER_NAME(582),
	// Message: Name includes forbidden words.
	NAME_INCLUDES_FORBIDDEN_WORDS(583),
	// Message: This is already in use by another pet.
	THIS_IS_ALREADY_IN_USE_BY_ANOTHER_PET(584),
	// Message: Please decide on the price.
	PLEASE_DECIDE_ON_THE_PRICE(585),
	// Message: Pet items cannot be registered as shortcuts.
	PET_ITEMS_CANNOT_BE_REGISTERED_AS_SHORTCUTS(586),
	// Message: Irregular system speed.
	IRREGULAR_SYSTEM_SPEED(587),
	// Message: Your pet's inventory is full.
	YOUR_PETS_INVENTORY_IS_FULL(588),
	// Message: A dead pet cannot be sent back.
	A_DEAD_PET_CANNOT_BE_SENT_BACK(589),
	// Message: Your pet is motionless and any attempt you make to give it something goes unrecognized.
	YOUR_PET_IS_MOTIONLESS_AND_ANY_ATTEMPT_YOU_MAKE_TO_GIVE_IT_SOMETHING_GOES_UNRECOGNIZED(590),
	// Message: An invalid character is included in the pet's name.
	AN_INVALID_CHARACTER_IS_INCLUDED_IN_THE_PETS_NAME(591),
	// Message: Do you wish to dismiss your pet? Dismissing your pet will cause the pet necklace to disappear.
	DO_YOU_WISH_TO_DISMISS_YOUR_PET_DISMISSING_YOUR_PET_WILL_CAUSE_THE_PET_NECKLACE_TO_DISAPPEAR(592),
	// Message: Starving, grumpy and fed up, your pet has left.
	STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT(593),
	// Message: You may not restore a hungry pet.
	YOU_MAY_NOT_RESTORE_A_HUNGRY_PET(594),
	// Message: Your pet is very hungry.
	YOUR_PET_IS_VERY_HUNGRY(595),
	// Message: Your pet ate a little, but is still hungry.
	YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY(596),
	// Message: Your pet is very hungry. Please be careful.
	YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL(597),
	// Message: You may not chat while you are invisible.
	YOU_MAY_NOT_CHAT_WHILE_YOU_ARE_INVISIBLE(598),
	// Message: The GM has an imprtant notice. Chat has been temporarily disabled.
	THE_GM_HAS_AN_IMPRTANT_NOTICE_CHAT_HAS_BEEN_TEMPORARILY_DISABLED(599),
	// Message: You may not equip a pet item.
	YOU_MAY_NOT_EQUIP_A_PET_ITEM(600),
	// Message: There are $S1 petitions currently on the waiting list.
	THERE_ARE_S1_PETITIONS_CURRENTLY_ON_THE_WAITING_LIST(601),
	// Message: The petition system is currently unavailable. Please try again later.
	THE_PETITION_SYSTEM_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER(602),
	// Message: That item cannot be discarded or exchanged.
	THAT_ITEM_CANNOT_BE_DISCARDED_OR_EXCHANGED(603),
	// Message: You may not call forth a pet or summoned creature from this location.
	YOU_MAY_NOT_CALL_FORTH_A_PET_OR_SUMMONED_CREATURE_FROM_THIS_LOCATION(604),
	// Message: You may register up to 64 people on your list.
	YOU_MAY_REGISTER_UP_TO_64_PEOPLE_ON_YOUR_LIST(605),
	// Message: You cannot be registered because the other person has already registered 64 people on his/her list.
	YOU_CANNOT_BE_REGISTERED_BECAUSE_THE_OTHER_PERSON_HAS_ALREADY_REGISTERED_64_PEOPLE_ON_HISHER_LIST(606),
	// Message: You do not have any further skills to learn.  Come back when you have reached Level $s1.
	YOU_DO_NOT_HAVE_ANY_FURTHER_SKILLS_TO_LEARN__COME_BACK_WHEN_YOU_HAVE_REACHED_LEVEL_S1(607),
	// Message: $s1 has obtained $s3 $s2  by using Sweeper.
	S1_HAS_OBTAINED_S3_S2__BY_USING_SWEEPER(608),
	// Message: $s1 has obtained $s2 by using Sweeper.
	S1_HAS_OBTAINED_S2_BY_USING_SWEEPER(609),
	// Message: Your skill has been canceled due to lack of HP.
	YOUR_SKILL_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_HP(610),
	// Message: You have succeeded in Confusing the enemy.
	YOU_HAVE_SUCCEEDED_IN_CONFUSING_THE_ENEMY(611),
	// Message: The Spoil condition has been activated.
	THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED(612),
	// Message: ======<Ignore List>======
	IGNORE_LIST(613),
	// Message: $s1 $s2
	S1_S2(614),
	// Message: You have failed to register the user to your Ignore List.
	YOU_HAVE_FAILED_TO_REGISTER_THE_USER_TO_YOUR_IGNORE_LIST(615),
	// Message: You have failed to delete the character.
	YOU_HAVE_FAILED_TO_DELETE_THE_CHARACTER_(616),
	// Message: $s1 has been added to your Ignore List.
	S1_HAS_BEEN_ADDED_TO_YOUR_IGNORE_LIST(617),
	// Message: $s1 has been removed from your Ignore List.
	S1_HAS_BEEN_REMOVED_FROM_YOUR_IGNORE_LIST(618),
	// Message: $s1  has placed you on his/her Ignore List.
	S1__HAS_PLACED_YOU_ON_HISHER_IGNORE_LIST(619),
	// Message: $s1  has placed you on his/her Ignore List.
	S1__HAS_PLACED_YOU_ON_HISHER_IGNORE_LIST_(620),
	// Message: This server is reserved for players in Korea.  To play Lineage II, please connect to the server in your region.
	THIS_SERVER_IS_RESERVED_FOR_PLAYERS_IN_KOREA__TO_PLAY_LINEAGE_II_PLEASE_CONNECT_TO_THE_SERVER_IN_YOUR_REGION(621),
	// Message: You may not make a declaration of war during an alliance battle.
	YOU_MAY_NOT_MAKE_A_DECLARATION_OF_WAR_DURING_AN_ALLIANCE_BATTLE(622),
	// Message: Your opponent has exceeded the number of simultaneous alliance battles allowed.
	YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED(623),
	// Message: $s1 Clan leader is not currently connected to the game server.
	S1_CLAN_LEADER_IS_NOT_CURRENTLY_CONNECTED_TO_THE_GAME_SERVER(624),
	// Message: Your request for Alliance Battle truce has been denied.
	YOUR_REQUEST_FOR_ALLIANCE_BATTLE_TRUCE_HAS_BEEN_DENIED(625),
	// Message: The $s1 clan did not respond: war proclamation has been refused.
	THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED_(626),
	// Message: Clan battle has been refused because you did not respond to $s1 clan's war proclamation.
	CLAN_BATTLE_HAS_BEEN_REFUSED_BECAUSE_YOU_DID_NOT_RESPOND_TO_S1_CLANS_WAR_PROCLAMATION(627),
	// Message: You have already been at war with the $s1 clan: 5 days must pass before you can declare war again.
	YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_DECLARE_WAR_AGAIN(628),
	// Message: Your opponent has exceeded the number of simultaneous alliance battles allowed.
	YOUR_OPPONENT_HAS_EXCEEDED_THE_NUMBER_OF_SIMULTANEOUS_ALLIANCE_BATTLES_ALLOWED_(629),
	// Message: War with the $s1 clan has begun.
	WAR_WITH_THE_S1_CLAN_HAS_BEGUN_(630),
	// Message: War with the $s1 clan is over.
	WAR_WITH_THE_S1_CLAN_IS_OVER(631),
	// Message: You have won the war over the $s1 clan!
	YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN_(632),
	// Message: You have surrendered to the $s1 clan.
	YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN_(633),
	// Message: Your alliance leader has been slain. You have been defeated by the $s1 clan.
	YOUR_ALLIANCE_LEADER_HAS_BEEN_SLAIN_YOU_HAVE_BEEN_DEFEATED_BY_THE_S1_CLAN(634),
	// Message: The time limit for the clan war has been exceeded. War with the $s1 clan is over.
	THE_TIME_LIMIT_FOR_THE_CLAN_WAR_HAS_BEEN_EXCEEDED_WAR_WITH_THE_S1_CLAN_IS_OVER(635),
	// Message: You are not involved in a clan war.
	YOU_ARE_NOT_INVOLVED_IN_A_CLAN_WAR_(636),
	// Message: A clan ally has registered itself to the opponent.
	A_CLAN_ALLY_HAS_REGISTERED_ITSELF_TO_THE_OPPONENT(637),
	// Message: You have already requested a Siege Battle.
	YOU_HAVE_ALREADY_REQUESTED_A_SIEGE_BATTLE(638),
	// Message: Your application has been denied because you have already submitted a request for another Siege Battle.
	YOUR_APPLICATION_HAS_BEEN_DENIED_BECAUSE_YOU_HAVE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE(639),
	// Message: You have failed to refuse castle defense aid.
	YOU_HAVE_FAILED_TO_REFUSE_CASTLE_DEFENSE_AID(640),
	// Message: You have failed to approve castle defense aid.
	YOU_HAVE_FAILED_TO_APPROVE_CASTLE_DEFENSE_AID(641),
	// Message: You are already registered to the attacker side and must cancel your registration before submitting your request.
	YOU_ARE_ALREADY_REGISTERED_TO_THE_ATTACKER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST(642),
	// Message: You have already registered to the defender side and must cancel your registration before submitting your request.
	YOU_HAVE_ALREADY_REGISTERED_TO_THE_DEFENDER_SIDE_AND_MUST_CANCEL_YOUR_REGISTRATION_BEFORE_SUBMITTING_YOUR_REQUEST(643),
	// Message: You are not yet registered for the castle siege.
	YOU_ARE_NOT_YET_REGISTERED_FOR_THE_CASTLE_SIEGE(644),
	// Message: Only clans of level 4 or higher may register for a castle siege.
	ONLY_CLANS_OF_LEVEL_4_OR_HIGHER_MAY_REGISTER_FOR_A_CASTLE_SIEGE(645),
	// Message: You do not have the authority to modify the castle defender list.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST(646),
	// Message: You do not have the authority to modify the siege time.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME(647),
	// Message: No more registrations may be accepted for the attacker side.
	NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_ATTACKER_SIDE(648),
	// Message: No more registrations may be accepted for the defender side.
	NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE(649),
	// Message: You may not summon from your current location.
	YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION(650),
	// Message: Place $s1 in the current location and direction. Do you wish to continue?
	PLACE_S1_IN_THE_CURRENT_LOCATION_AND_DIRECTION_DO_YOU_WISH_TO_CONTINUE(651),
	// Message: The target of the summoned monster is wrong.
	THE_TARGET_OF_THE_SUMMONED_MONSTER_IS_WRONG(652),
	// Message: You do not have the authority to position mercenaries.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_POSITION_MERCENARIES(653),
	// Message: You do not have the authority to cancel mercenary positioning.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING(654),
	// Message: Mercenaries cannot be positioned here.
	MERCENARIES_CANNOT_BE_POSITIONED_HERE(655),
	// Message: This mercenary cannot be positioned anymore.
	THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE(656),
	// Message: Positioning cannot be done here because the distance between mercenaries is too short.
	POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT(657),
	// Message: This is not a mercenary of a castle that you own and so you cannot cancel its positioning.
	THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_YOU_CANNOT_CANCEL_ITS_POSITIONING(658),
	// Message: This is not the time for siege registration and so registrations cannot be accepted or rejected.
	THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED(659),
	// Message: This is not the time for siege registration and so registration and cancellation cannot be done.
	THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATION_AND_CANCELLATION_CANNOT_BE_DONE(660),
	// Message: This character cannot be spoiled.
	THIS_CHARACTER_CANNOT_BE_SPOILED(661),
	// Message: The other player is rejecting friend invitations.
	THE_OTHER_PLAYER_IS_REJECTING_FRIEND_INVITATIONS(662),
	// Message: The siege time has been declared for $s. It is not possible to change the time after a siege time has been declared. Do you want to continue?
	THE_SIEGE_TIME_HAS_BEEN_DECLARED_FOR_S_IT_IS_NOT_POSSIBLE_TO_CHANGE_THE_TIME_AFTER_A_SIEGE_TIME_HAS_BEEN_DECLARED_DO_YOU_WANT_TO_CONTINUE(663),
	// Message: Please choose a person to receive.
	PLEASE_CHOOSE_A_PERSON_TO_RECEIVE(664),
	// Message: $s2 of $s1 alliance is applying for alliance war. Do you want to accept the challenge?
	S2_OF_S1_ALLIANCE_IS_APPLYING_FOR_ALLIANCE_WAR_DO_YOU_WANT_TO_ACCEPT_THE_CHALLENGE(665),
	// Message: A request for ceasefire has been received from $s1 alliance. Do you agree?
	A_REQUEST_FOR_CEASEFIRE_HAS_BEEN_RECEIVED_FROM_S1_ALLIANCE_DO_YOU_AGREE(666),
	// Message: You are registering on the attacking side of the $s1 siege. Do you want to continue?
	YOU_ARE_REGISTERING_ON_THE_ATTACKING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE(667),
	// Message: You are registering on the defending side of the $s1 siege. Do you want to continue?
	YOU_ARE_REGISTERING_ON_THE_DEFENDING_SIDE_OF_THE_S1_SIEGE_DO_YOU_WANT_TO_CONTINUE(668),
	// Message: You are canceling your application to participate in the $s1 siege battle. Do you want to continue?
	YOU_ARE_CANCELING_YOUR_APPLICATION_TO_PARTICIPATE_IN_THE_S1_SIEGE_BATTLE_DO_YOU_WANT_TO_CONTINUE(669),
	// Message: You are refusing the registration of $s1 clan on the defending side. Do you want to continue?
	YOU_ARE_REFUSING_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE(670),
	// Message: You are agreeing to the registration of $s1 clan on the defending side. Do you want to continue?
	YOU_ARE_AGREEING_TO_THE_REGISTRATION_OF_S1_CLAN_ON_THE_DEFENDING_SIDE_DO_YOU_WANT_TO_CONTINUE(671),
	// Message: $s1 adena disappeared.
	S1_ADENA_DISAPPEARED(672),
	// Message: Only a clan leader whose clan is of level 2 or higher is allowed to participate in a clan hall auction.
	ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION(673),
	// Message: It has not yet been seven days since canceling an auction.
	IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION(674),
	// Message: There are no clan halls up for auction.
	THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION(675),
	// Message: Since you have already submitted a bid, you are not allowed to participate in another auction at this time.
	SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME(676),
	// Message: Your bid price must be higher than the minimum price that can be bid.
	YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_THAT_CAN_BE_BID(677),
	// Message: You have submitted a bid in the auction of $s1.
	YOU_HAVE_SUBMITTED_A_BID_IN_THE_AUCTION_OF_S1(678),
	// Message: You have canceled your bid.
	YOU_HAVE_CANCELED_YOUR_BID(679),
	// Message: You cannot participate in an auction.
	YOU_CANNOT_PARTICIPATE_IN_AN_AUCTION(680),
	// Message: The clan does not own a clan hall.
	THE_CLAN_DOES_NOT_OWN_A_CLAN_HALL(681),
	// Message: You are moving to another village. Do you want to continue?
	YOU_ARE_MOVING_TO_ANOTHER_VILLAGE_DO_YOU_WANT_TO_CONTINUE(682),
	// Message: There are no priority rights on a sweeper.
	THERE_ARE_NO_PRIORITY_RIGHTS_ON_A_SWEEPER(683),
	// Message: You cannot position mercenaries during a siege.
	YOU_CANNOT_POSITION_MERCENARIES_DURING_A_SIEGE(684),
	// Message: You cannot apply for clan war with a clan that belongs to the same alliance.
	YOU_CANNOT_APPLY_FOR_CLAN_WAR_WITH_A_CLAN_THAT_BELONGS_TO_THE_SAME_ALLIANCE(685),
	// Message: You have received $s1 damage from the fire of magic.
	YOU_HAVE_RECEIVED_S1_DAMAGE_FROM_THE_FIRE_OF_MAGIC(686),
	// Message: You cannot move while frozen. Please wait.
	YOU_CANNOT_MOVE_WHILE_FROZEN_PLEASE_WAIT(687),
	// Message: The clan that owns the castle is automatically registered on the defending side.
	THE_CLAN_THAT_OWNS_THE_CASTLE_IS_AUTOMATICALLY_REGISTERED_ON_THE_DEFENDING_SIDE(688),
	// Message: A clan that owns a castle cannot participate in another siege.
	A_CLAN_THAT_OWNS_A_CASTLE_CANNOT_PARTICIPATE_IN_ANOTHER_SIEGE(689),
	// Message: You cannot register on the attacking side because you are part of an alliance with the clan that owns the castle.
	YOU_CANNOT_REGISTER_ON_THE_ATTACKING_SIDE_BECAUSE_YOU_ARE_PART_OF_AN_ALLIANCE_WITH_THE_CLAN_THAT_OWNS_THE_CASTLE(690),
	// Message: $s1 clan is already a member of $s2 alliance.
	S1_CLAN_IS_ALREADY_A_MEMBER_OF_S2_ALLIANCE(691),
	// Message: The other party is frozen. Please wait a moment.
	THE_OTHER_PARTY_IS_FROZEN_PLEASE_WAIT_A_MOMENT(692),
	// Message: The package that arrived is in another warehouse.
	THE_PACKAGE_THAT_ARRIVED_IS_IN_ANOTHER_WAREHOUSE(693),
	// Message: No packages have arrived.
	NO_PACKAGES_HAVE_ARRIVED(694),
	// Message: You cannot set the name of the pet.
	YOU_CANNOT_SET_THE_NAME_OF_THE_PET(695),
	// Message: Your account is restricted for not paying your PC room usage fees.
	YOUR_ACCOUNT_IS_RESTRICTED_FOR_NOT_PAYING_YOUR_PC_ROOM_USAGE_FEES(696),
	// Message: The item enchant value is strange.
	THE_ITEM_ENCHANT_VALUE_IS_STRANGE(697),
	// Message: The price is different than the same item on the sales list.
	THE_PRICE_IS_DIFFERENT_THAN_THE_SAME_ITEM_ON_THE_SALES_LIST(698),
	// Message: Currently not purchasing.
	CURRENTLY_NOT_PURCHASING(699),
	// Message: The purchase is complete.
	THE_PURCHASE_IS_COMPLETE(700),
	// Message: You do not have enough required items.
	YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS(701),
	// Message: There are no GMs currently visible in the public list as they may be performing other functions at the moment.
	THERE_ARE_NO_GMS_CURRENTLY_VISIBLE_IN_THE_PUBLIC_LIST_AS_THEY_MAY_BE_PERFORMING_OTHER_FUNCTIONS_AT_THE_MOMENT(702),
	// Message: ======<GM List>======
	GM_LIST(703),
	// Message: GM : $s1
	GM__S1(704),
	// Message: You cannot exclude yourself.
	YOU_CANNOT_EXCLUDE_YOURSELF(705),
	// Message: You can only register up to 64 names on your exclude list.
	YOU_CAN_ONLY_REGISTER_UP_TO_64_NAMES_ON_YOUR_EXCLUDE_LIST(706),
	// Message: You cannot teleport to a village that is in a siege.
	YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE(707),
	// Message: You do not have the right to use the castle warehouse.
	YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CASTLE_WAREHOUSE(708),
	// Message: You do not have the right to use the clan warehouse.
	YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_THE_CLAN_WAREHOUSE(709),
	// Message: Only clans of clan level 1 or higher can use a clan warehouse.
	ONLY_CLANS_OF_CLAN_LEVEL_1_OR_HIGHER_CAN_USE_A_CLAN_WAREHOUSE(710),
	// Message: The siege of $s1 has started.
	THE_SIEGE_OF_S1_HAS_STARTED(711),
	// Message: The siege of $s1 has finished.
	THE_SIEGE_OF_S1_HAS_FINISHED(712),
	// Message: $s1/$s2/$s3 $s4:$s5
	S1S2S3_S4S5(713),
	// Message: A trap device has been tripped.
	A_TRAP_DEVICE_HAS_BEEN_TRIPPED(714),
	// Message: The trap device has been stopped.
	THE_TRAP_DEVICE_HAS_BEEN_STOPPED(715),
	// Message: If a base camp does not exist, resurrection is not possible.
	IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE(716),
	// Message: The guardian tower has been destroyed and resurrection is not possible.
	THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE(717),
	// Message: The castle gates cannot be opened and closed during a siege.
	THE_CASTLE_GATES_CANNOT_BE_OPENED_AND_CLOSED_DURING_A_SIEGE(718),
	// Message: You failed at mixing the item.
	YOU_FAILED_AT_MIXING_THE_ITEM(719),
	// Message: The purchase price is higher than the amount of money that you have and so you cannot open a personal store.
	THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE(720),
	// Message: You cannot create an alliance while participating in a siege.
	YOU_CANNOT_CREATE_AN_ALLIANCE_WHILE_PARTICIPATING_IN_A_SIEGE(721),
	// Message: You cannot dissolve an alliance while an affiliated clan is participating in a siege battle.
	YOU_CANNOT_DISSOLVE_AN_ALLIANCE_WHILE_AN_AFFILIATED_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE(722),
	// Message: The opposing clan is participating in a siege battle.
	THE_OPPOSING_CLAN_IS_PARTICIPATING_IN_A_SIEGE_BATTLE(723),
	// Message: You cannot leave while participating in a siege battle.
	YOU_CANNOT_LEAVE_WHILE_PARTICIPATING_IN_A_SIEGE_BATTLE(724),
	// Message: You cannot banish a clan from an alliance while the clan is participating in a siege.
	YOU_CANNOT_BANISH_A_CLAN_FROM_AN_ALLIANCE_WHILE_THE_CLAN_IS_PARTICIPATING_IN_A_SIEGE(725),
	// Message: The frozen condition has started. Please wait a moment.
	THE_FROZEN_CONDITION_HAS_STARTED_PLEASE_WAIT_A_MOMENT(726),
	// Message: The frozen condition was removed.
	THE_FROZEN_CONDITION_WAS_REMOVED(727),
	// Message: You cannot apply for dissolution again within seven days after a previous application for dissolution.
	YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION(728),
	// Message: That item cannot be discarded.
	THAT_ITEM_CANNOT_BE_DISCARDED(729),
	// Message: - You have submitted your $s1th petition. \\n - You may submit $s2 more petitions today.
	_YOU_HAVE_SUBMITTED_YOUR_S1TH_PETITION_N__YOU_MAY_SUBMIT_S2_MORE_PETITIONS_TODAY(730),
	// Message: A petition has been received by the GM on behalf of $s1. It is petition #$s2.
	A_PETITION_HAS_BEEN_RECEIVED_BY_THE_GM_ON_BEHALF_OF_S1_IT_IS_PETITION_S2(731),
	// Message: $s1 has received a request for a consultation with the GM.
	S1_HAS_RECEIVED_A_REQUEST_FOR_A_CONSULTATION_WITH_THE_GM(732),
	// Message: We have received $s1 petitions from you today and that is the maximum that you can submit in one day. You cannot submit any more petitions.
	WE_HAVE_RECEIVED_S1_PETITIONS_FROM_YOU_TODAY_AND_THAT_IS_THE_MAXIMUM_THAT_YOU_CAN_SUBMIT_IN_ONE_DAY_YOU_CANNOT_SUBMIT_ANY_MORE_PETITIONS(733),
	// Message: You failed at submitting a petition on behalf of someone else. $s1 already submitted a petition.
	YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_SOMEONE_ELSE_S1_ALREADY_SUBMITTED_A_PETITION(734),
	// Message: You failed at submitting a petition on behalf of $s1. The error is #$s2.
	YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_IS_S2(735),
	// Message: The petition was canceled. You may submit $s1 more petitions today.
	THE_PETITION_WAS_CANCELED_YOU_MAY_SUBMIT_S1_MORE_PETITIONS_TODAY(736),
	// Message: You failed at submitting a petition on behalf of $s1.
	YOU_FAILED_AT_SUBMITTING_A_PETITION_ON_BEHALF_OF_S1(737),
	// Message: You have not submitted a petition.
	YOU_HAVE_NOT_SUBMITTED_A_PETITION(738),
	// Message: You failed at canceling a petition on behalf of $s1. The error code is $s2.
	YOU_FAILED_AT_CANCELING_A_PETITION_ON_BEHALF_OF_S1_THE_ERROR_CODE_IS_S2(739),
	// Message: $s1 participated in a petition chat at the request of the GM.
	S1_PARTICIPATED_IN_A_PETITION_CHAT_AT_THE_REQUEST_OF_THE_GM(740),
	// Message: You failed at adding $s1 to the petition chat. A petition has already been submitted.
	YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_A_PETITION_HAS_ALREADY_BEEN_SUBMITTED(741),
	// Message: You failed at adding $s1 to the petition chat. The error code is $s2.
	YOU_FAILED_AT_ADDING_S1_TO_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2(742),
	// Message: $s1 left the petition chat.
	S1_LEFT_THE_PETITION_CHAT(743),
	// Message: You failed at removing $s1 from the petition chat. The error code is $s2.
	YOU_FAILED_AT_REMOVING_S1_FROM_THE_PETITION_CHAT_THE_ERROR_CODE_IS_S2(744),
	// Message: You are currently not in a petition chat.
	YOU_ARE_CURRENTLY_NOT_IN_A_PETITION_CHAT(745),
	// Message: It is not currently a petition.
	IT_IS_NOT_CURRENTLY_A_PETITION(746),
	// Message: If you need help, please use 1:1 Inquiry on the official web site.
	IF_YOU_NEED_HELP_PLEASE_USE_11_INQUIRY_ON_THE_OFFICIAL_WEB_SITE(747),
	// Message: The distance is too far and so the casting has been stopped.
	THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED(748),
	// Message: The effect of $s1 has been removed.
	THE_EFFECT_OF_S1_HAS_BEEN_REMOVED(749),
	// Message: There are no other skills to learn.
	THERE_ARE_NO_OTHER_SKILLS_TO_LEARN(750),
	// Message: As there is a conflict in the siege relationship with a clan in the alliance, you cannot invite that clan to the alliance.
	AS_THERE_IS_A_CONFLICT_IN_THE_SIEGE_RELATIONSHIP_WITH_A_CLAN_IN_THE_ALLIANCE_YOU_CANNOT_INVITE_THAT_CLAN_TO_THE_ALLIANCE(751),
	// Message: That name cannot be used.
	THAT_NAME_CANNOT_BE_USED(752),
	// Message: You cannot position mercenaries here.
	YOU_CANNOT_POSITION_MERCENARIES_HERE(753),
	// Message: There are $s1 hours and $s2 minutes left in this week's usage time.
	THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THIS_WEEKS_USAGE_TIME(754),
	// Message: There are $s1 minutes left in this week's usage time.
	THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEKS_USAGE_TIME(755),
	// Message: This week's usage time has finished.
	THIS_WEEKS_USAGE_TIME_HAS_FINISHED(756),
	// Message: There are $s1 hours and $s2 minutes left in the fixed use time.
	THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THE_FIXED_USE_TIME(757),
	// Message: There are $s1 minutes left in this week's play time.
	THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEKS_PLAY_TIME(758),
	// Message: There are $s1 minutes left in this week's play time.
	THERE_ARE_S1_MINUTES_LEFT_IN_THIS_WEEKS_PLAY_TIME_(759),
	// Message: $s1 cannot join the clan because one day has not yet passed since he/she left another clan.
	S1_CANNOT_JOIN_THE_CLAN_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_HESHE_LEFT_ANOTHER_CLAN(760),
	// Message: $s1 clan cannot join the alliance because one day has not yet passed since it left another alliance.
	S1_CLAN_CANNOT_JOIN_THE_ALLIANCE_BECAUSE_ONE_DAY_HAS_NOT_YET_PASSED_SINCE_IT_LEFT_ANOTHER_ALLIANCE(761),
	// Message: $s1 rolled $s2 and $s3's eye came out.
	S1_ROLLED_S2_AND_S3S_EYE_CAME_OUT(762),
	// Message: You failed at sending the package because you are too far from the warehouse.
	YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE(763),
	// Message: You have been playing for an extended period of time. Please consider taking a break.
	YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK(764),
	// Message: GameGuard is already running. Please try running it again after rebooting.
	GAMEGUARD_IS_ALREADY_RUNNING_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING(765),
	// Message: There is a GameGuard initialization error. Please try running it again after rebooting.
	THERE_IS_A_GAMEGUARD_INITIALIZATION_ERROR_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_REBOOTING(766),
	// Message: The GameGuard file is damaged . Please reinstall GameGuard.
	THE_GAMEGUARD_FILE_IS_DAMAGED__PLEASE_REINSTALL_GAMEGUARD(767),
	// Message: A Windows system file is damaged. Please reinstall Internet Explorer.
	A_WINDOWS_SYSTEM_FILE_IS_DAMAGED_PLEASE_REINSTALL_INTERNET_EXPLORER(768),
	// Message: A hacking tool has been discovered. Please try playing again after closing unnecessary programs.
	A_HACKING_TOOL_HAS_BEEN_DISCOVERED_PLEASE_TRY_PLAYING_AGAIN_AFTER_CLOSING_UNNECESSARY_PROGRAMS(769),
	// Message: The GameGuard update was canceled. Please check your network connection status or firewall.
	THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_CHECK_YOUR_NETWORK_CONNECTION_STATUS_OR_FIREWALL(770),
	// Message: The GameGuard update was canceled. Please try running it again after doing a virus scan or changing the settings in your PC management program.
	THE_GAMEGUARD_UPDATE_WAS_CANCELED_PLEASE_TRY_RUNNING_IT_AGAIN_AFTER_DOING_A_VIRUS_SCAN_OR_CHANGING_THE_SETTINGS_IN_YOUR_PC_MANAGEMENT_PROGRAM(771),
	// Message: There was a problem when running GameGuard.
	THERE_WAS_A_PROBLEM_WHEN_RUNNING_GAMEGUARD(772),
	// Message: The game or GameGuard files are damaged.
	THE_GAME_OR_GAMEGUARD_FILES_ARE_DAMAGED(773),
	// Message: Play time is no longer accumulating.
	PLAY_TIME_IS_NO_LONGER_ACCUMULATING(774),
	// Message: From here on, play time will be expended.
	FROM_HERE_ON_PLAY_TIME_WILL_BE_EXPENDED(775),
	// Message: The clan hall which was put up for auction has been awarded to $s1 clan.
	THE_CLAN_HALL_WHICH_WAS_PUT_UP_FOR_AUCTION_HAS_BEEN_AWARDED_TO_S1_CLAN(776),
	// Message: The clan hall which had been put up for auction was not sold and therefore has been re-listed.
	THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED(777),
	// Message: You may not log out from this location.
	YOU_MAY_NOT_LOG_OUT_FROM_THIS_LOCATION(778),
	// Message: You may not restart in this location.
	YOU_MAY_NOT_RESTART_IN_THIS_LOCATION(779),
	// Message: Observation is only possible during a siege.
	OBSERVATION_IS_ONLY_POSSIBLE_DURING_A_SIEGE(780),
	// Message: Observers cannot participate.
	OBSERVERS_CANNOT_PARTICIPATE(781),
	// Message: You may not observe a summoned creature.
	YOU_MAY_NOT_OBSERVE_A_SUMMONED_CREATURE(782),
	// Message: Lottery ticket sales have been temporarily suspended.
	LOTTERY_TICKET_SALES_HAVE_BEEN_TEMPORARILY_SUSPENDED(783),
	// Message: Tickets for the current lottery are no longer available.
	TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE(784),
	// Message: The results of lottery number $s1 have not yet been published.
	THE_RESULTS_OF_LOTTERY_NUMBER_S1_HAVE_NOT_YET_BEEN_PUBLISHED(785),
	// Message: Incorrect syntax.
	INCORRECT_SYNTAX(786),
	// Message: The tryouts are finished.
	THE_TRYOUTS_ARE_FINISHED(787),
	// Message: The finals are finished.
	THE_FINALS_ARE_FINISHED(788),
	// Message: The tryouts have begun.
	THE_TRYOUTS_HAVE_BEGUN(789),
	// Message: The finals have begun.
	THE_FINALS_HAVE_BEGUN(790),
	// Message: The final match is about to begin. Line up!
	THE_FINAL_MATCH_IS_ABOUT_TO_BEGIN_LINE_UP(791),
	// Message: The siege of the clan hall is finished.
	THE_SIEGE_OF_THE_CLAN_HALL_IS_FINISHED(792),
	// Message: The siege of the clan hall has begun.
	THE_SIEGE_OF_THE_CLAN_HALL_HAS_BEGUN(793),
	// Message: You are not authorized to do that.
	YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT(794),
	// Message: Only clan leaders are authorized to set rights.
	ONLY_CLAN_LEADERS_ARE_AUTHORIZED_TO_SET_RIGHTS(795),
	// Message: Your remaining observation time is $s1 minutes.
	YOUR_REMAINING_OBSERVATION_TIME_IS_S1_MINUTES(796),
	// Message: You may create up to 24 macros.
	YOU_MAY_CREATE_UP_TO_24_MACROS(797),
	// Message: Item registration is irreversible. Do you wish to continue?
	ITEM_REGISTRATION_IS_IRREVERSIBLE_DO_YOU_WISH_TO_CONTINUE(798),
	// Message: The observation time has expired.
	THE_OBSERVATION_TIME_HAS_EXPIRED(799),
	// Message: You are too late. The registration period is over.
	YOU_ARE_TOO_LATE_THE_REGISTRATION_PERIOD_IS_OVER(800),
	// Message: Registration for the clan hall siege is closed.
	REGISTRATION_FOR_THE_CLAN_HALL_SIEGE_IS_CLOSED(801),
	// Message: Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.
	PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_AMPM(802),
	// Message: Enter the specifics of your petition.
	ENTER_THE_SPECIFICS_OF_YOUR_PETITION(803),
	// Message: Select a type.
	SELECT_A_TYPE(804),
	// Message: Petitions are not being accepted at this time. You may submit your petition after $s1 a.m./p.m.
	PETITIONS_ARE_NOT_BEING_ACCEPTED_AT_THIS_TIME_YOU_MAY_SUBMIT_YOUR_PETITION_AFTER_S1_AMPM_(805),
	// Message: If you are trapped, try typing "/unstuck".
	IF_YOU_ARE_TRAPPED_TRY_TYPING_UNSTUCK(806),
	// Message: This terrain is navigable. Prepare for transport to the nearest village.
	THIS_TERRAIN_IS_NAVIGABLE_PREPARE_FOR_TRANSPORT_TO_THE_NEAREST_VILLAGE(807),
	// Message: You are stuck. You may submit a petition by typing "/gm".
	YOU_ARE_STUCK_YOU_MAY_SUBMIT_A_PETITION_BY_TYPING_GM(808),
	// Message: You are stuck. You will be transported to the nearest village in five minutes.
	YOU_ARE_STUCK_YOU_WILL_BE_TRANSPORTED_TO_THE_NEAREST_VILLAGE_IN_FIVE_MINUTES(809),
	// Message: Invalid macro. Refer to the Help file for instructions.
	INVALID_MACRO_REFER_TO_THE_HELP_FILE_FOR_INSTRUCTIONS(810),
	// Message: You will be moved to ($s1). Do you wish to continue?
	YOU_WILL_BE_MOVED_TO_S1_DO_YOU_WISH_TO_CONTINUE(811),
	// Message: The secret trap has inflicted $s1 damage on you.
	THE_SECRET_TRAP_HAS_INFLICTED_S1_DAMAGE_ON_YOU(812),
	// Message: You have been poisoned by a Secret Trap.
	YOU_HAVE_BEEN_POISONED_BY_A_SECRET_TRAP(813),
	// Message: Your speed has been decreased by a Secret Trap.
	YOUR_SPEED_HAS_BEEN_DECREASED_BY_A_SECRET_TRAP(814),
	// Message: The tryouts are about to begin. Line up!
	THE_TRYOUTS_ARE_ABOUT_TO_BEGIN_LINE_UP(815),
	// Message: Tickets are now available for Monster Race $s1!
	TICKETS_ARE_NOW_AVAILABLE_FOR_MONSTER_RACE_S1(816),
	// Message: Now selling tickets for Monster Race $s1!
	NOW_SELLING_TICKETS_FOR_MONSTER_RACE_S1(817),
	// Message: Ticket sales for the Monster Race will end in $s1 minute(s).
	TICKET_SALES_FOR_THE_MONSTER_RACE_WILL_END_IN_S1_MINUTES(818),
	// Message: Tickets sales are closed for Monster Race $s1. Odds are posted.
	TICKETS_SALES_ARE_CLOSED_FOR_MONSTER_RACE_S1_ODDS_ARE_POSTED(819),
	// Message: Monster Race $s2 will begin in $s1 minute(s)!
	MONSTER_RACE_S2_WILL_BEGIN_IN_S1_MINUTES(820),
	// Message: Monster Race $s1 will begin in 30 seconds!
	MONSTER_RACE_S1_WILL_BEGIN_IN_30_SECONDS(821),
	// Message: Monster Race $s1 is about to begin! Countdown in five seconds!
	MONSTER_RACE_S1_IS_ABOUT_TO_BEGIN_COUNTDOWN_IN_FIVE_SECONDS(822),
	// Message: The race will begin in $s1 second(s)!
	THE_RACE_WILL_BEGIN_IN_S1_SECONDS(823),
	// Message: They're off!
	THEYRE_OFF(824),
	// Message: Monster Race $s1 is finished!
	MONSTER_RACE_S1_IS_FINISHED(825),
	// Message: First prize goes to the player in lane $s1. Second prize goes to the player in lane $s2.
	FIRST_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S1_SECOND_PRIZE_GOES_TO_THE_PLAYER_IN_LANE_S2(826),
	// Message: You may not impose a block on a GM.
	YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_A_GM(827),
	// Message: Are you sure you wish to delete the $s1 macro?
	ARE_YOU_SURE_YOU_WISH_TO_DELETE_THE_S1_MACRO(828),
	// Message: You cannot recommend yourself.
	YOU_CANNOT_RECOMMEND_YOURSELF(829),
	// Message: You have recommended $s1. You are authorized to make $s2 more recommendations.
	YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS(830),
	// Message: You have been recommended by $s1.
	YOU_HAVE_BEEN_RECOMMENDED_BY_S1(831),
	// Message: That character has already been recommended.
	THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED(832),
	// Message: You are not authorized to make further recommendations at this time. You will receive more recommendation credits each day at 1 p.m.
	YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME_YOU_WILL_RECEIVE_MORE_RECOMMENDATION_CREDITS_EACH_DAY_AT_1_PM(833),
	// Message: $s1 has rolled $s2.
	S1_HAS_ROLLED_S2(834),
	// Message: You may not throw the dice at this time. Try again later.
	YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER(835),
	// Message: You have exceeded your inventory volume limit and cannot take this item.
	YOU_HAVE_EXCEEDED_YOUR_INVENTORY_VOLUME_LIMIT_AND_CANNOT_TAKE_THIS_ITEM(836),
	// Message: Macro descriptions may contain up to 32 characters.
	MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS(837),
	// Message: Enter the name of the macro.
	ENTER_THE_NAME_OF_THE_MACRO(838),
	// Message: That name is already assigned to another macro.
	THAT_NAME_IS_ALREADY_ASSIGNED_TO_ANOTHER_MACRO(839),
	// Message: That recipe is already registered.
	THAT_RECIPE_IS_ALREADY_REGISTERED(840),
	// Message: No further recipes may be registered.
	NO_FURTHER_RECIPES_MAY_BE_REGISTERED(841),
	// Message: You are not authorized to register a recipe.
	YOU_ARE_NOT_AUTHORIZED_TO_REGISTER_A_RECIPE(842),
	// Message: The siege of $s1 is finished.
	THE_SIEGE_OF_S1_IS_FINISHED(843),
	// Message: The siege to conquer $s1 has begun.
	THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN(844),
	// Message: The deadline to register for the siege of $s1 has passed.
	THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED(845),
	// Message: The siege of $s1 has been canceled due to lack of interest.
	THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST(846),
	// Message: A clan that owns a clan hall may not participate in a clan hall siege.
	A_CLAN_THAT_OWNS_A_CLAN_HALL_MAY_NOT_PARTICIPATE_IN_A_CLAN_HALL_SIEGE(847),
	// Message: $s1 has been deleted.
	S1_HAS_BEEN_DELETED(848),
	// Message: $s1 cannot be found.
	S1_CANNOT_BE_FOUND(849),
	// Message: $s1 already exists.
	S1_ALREADY_EXISTS_(850),
	// Message: $s1 has been added.
	S1_HAS_BEEN_ADDED(851),
	// Message: The recipe is incorrect.
	THE_RECIPE_IS_INCORRECT(852),
	// Message: You may not alter your recipe book while engaged in manufacturing.
	YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING(853),
	// Message: You are missing $s2 $s1 required to create that.
	YOU_ARE_MISSING_S2_S1_REQUIRED_TO_CREATE_THAT(854),
	// Message: $s1 clan has defeated $s2.
	S1_CLAN_HAS_DEFEATED_S2(855),
	// Message: The siege of $s1 has ended in a draw.
	THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW(856),
	// Message: $s1 clan has won in the preliminary match of $s2.
	S1_CLAN_HAS_WON_IN_THE_PRELIMINARY_MATCH_OF_S2(857),
	// Message: The preliminary match of $s1 has ended in a draw.
	THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW(858),
	// Message: Please register a recipe.
	PLEASE_REGISTER_A_RECIPE(859),
	// Message: You may not build your headquarters in close proximity to another headquarters.
	YOU_MAY_NOT_BUILD_YOUR_HEADQUARTERS_IN_CLOSE_PROXIMITY_TO_ANOTHER_HEADQUARTERS(860),
	// Message: You have exceeded the maximum number of memos.
	YOU_HAVE_EXCEEDED_THE_MAXIMUM_NUMBER_OF_MEMOS(861),
	// Message: Odds are not posted until ticket sales have closed.
	ODDS_ARE_NOT_POSTED_UNTIL_TICKET_SALES_HAVE_CLOSED(862),
	// Message: You feel the energy of fire.
	YOU_FEEL_THE_ENERGY_OF_FIRE(863),
	// Message: You feel the energy of water.
	YOU_FEEL_THE_ENERGY_OF_WATER(864),
	// Message: You feel the energy of wind.
	YOU_FEEL_THE_ENERGY_OF_WIND(865),
	// Message: You may no longer gather energy.
	YOU_MAY_NO_LONGER_GATHER_ENERGY(866),
	// Message: The energy is depleted.
	THE_ENERGY_IS_DEPLETED(867),
	// Message: The energy of fire has been delivered.
	THE_ENERGY_OF_FIRE_HAS_BEEN_DELIVERED(868),
	// Message: The energy of water has been delivered.
	THE_ENERGY_OF_WATER_HAS_BEEN_DELIVERED(869),
	// Message: The energy of wind has been delivered.
	THE_ENERGY_OF_WIND_HAS_BEEN_DELIVERED(870),
	// Message: The seed has been sown.
	THE_SEED_HAS_BEEN_SOWN(871),
	// Message: This seed may not be sown here.
	THIS_SEED_MAY_NOT_BE_SOWN_HERE(872),
	// Message: That character does not exist.
	THAT_CHARACTER_DOES_NOT_EXIST(873),
	// Message: The capacity of the warehouse has been exceeded.
	THE_CAPACITY_OF_THE_WAREHOUSE_HAS_BEEN_EXCEEDED(874),
	// Message: The transport of the cargo has been canceled.
	THE_TRANSPORT_OF_THE_CARGO_HAS_BEEN_CANCELED(875),
	// Message: The cargo was not delivered.
	THE_CARGO_WAS_NOT_DELIVERED(876),
	// Message: The symbol has been added.
	THE_SYMBOL_HAS_BEEN_ADDED(877),
	// Message: The symbol has been deleted.
	THE_SYMBOL_HAS_BEEN_DELETED(878),
	// Message: The manor system is currently under maintenance.
	THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE(879),
	// Message: The transaction is complete.
	THE_TRANSACTION_IS_COMPLETE(880),
	// Message: There is a discrepancy on the invoice.
	THERE_IS_A_DISCREPANCY_ON_THE_INVOICE(881),
	// Message: The seed quantity is incorrect.
	THE_SEED_QUANTITY_IS_INCORRECT(882),
	// Message: The seed information is incorrect.
	THE_SEED_INFORMATION_IS_INCORRECT(883),
	// Message: The manor information has been updated.
	THE_MANOR_INFORMATION_HAS_BEEN_UPDATED(884),
	// Message: The number of crops is incorrect.
	THE_NUMBER_OF_CROPS_IS_INCORRECT(885),
	// Message: The crops are priced incorrectly.
	THE_CROPS_ARE_PRICED_INCORRECTLY(886),
	// Message: The type is incorrect.
	THE_TYPE_IS_INCORRECT(887),
	// Message: No crops can be purchased at this time.
	NO_CROPS_CAN_BE_PURCHASED_AT_THIS_TIME(888),
	// Message: The seed was successfully sown.
	THE_SEED_WAS_SUCCESSFULLY_SOWN(889),
	// Message: The seed was not sown.
	THE_SEED_WAS_NOT_SOWN(890),
	// Message: You are not authorized to harvest.
	YOU_ARE_NOT_AUTHORIZED_TO_HARVEST(891),
	// Message: The harvest has failed.
	THE_HARVEST_HAS_FAILED(892),
	// Message: The harvest failed because the seed was not sown.
	THE_HARVEST_FAILED_BECAUSE_THE_SEED_WAS_NOT_SOWN(893),
	// Message: Up to $s1 recipes can be registered.
	UP_TO_S1_RECIPES_CAN_BE_REGISTERED(894),
	// Message: No recipes have been registered.
	NO_RECIPES_HAVE_BEEN_REGISTERED(895),
	// Message: Quest recipes can not be registered.
	QUEST_RECIPES_CAN_NOT_BE_REGISTERED(896),
	// Message: The fee to create the item is incorrect.
	THE_FEE_TO_CREATE_THE_ITEM_IS_INCORRECT(897),
	// Message: Only characters of level 10 or above are authorized to make recommendations.
	ONLY_CHARACTERS_OF_LEVEL_10_OR_ABOVE_ARE_AUTHORIZED_TO_MAKE_RECOMMENDATIONS(898),
	// Message: The symbol cannot be drawn.
	THE_SYMBOL_CANNOT_BE_DRAWN(899),
	// Message: No slot exists to draw the symbol.
	NO_SLOT_EXISTS_TO_DRAW_THE_SYMBOL(900),
	// Message: The symbol information cannot be found.
	THE_SYMBOL_INFORMATION_CANNOT_BE_FOUND(901),
	// Message: The number of items is incorrect.
	THE_NUMBER_OF_ITEMS_IS_INCORRECT(902),
	// Message: You may not submit a petition while frozen. Be patient.
	YOU_MAY_NOT_SUBMIT_A_PETITION_WHILE_FROZEN_BE_PATIENT(903),
	// Message: Items cannot be discarded while in private store status.
	ITEMS_CANNOT_BE_DISCARDED_WHILE_IN_PRIVATE_STORE_STATUS(904),
	// Message: The current score for the Humans is $s1.
	THE_CURRENT_SCORE_FOR_THE_HUMANS_IS_S1(905),
	// Message: The current score for the Elves is $s1.
	THE_CURRENT_SCORE_FOR_THE_ELVES_IS_S1(906),
	// Message: The current score for the Dark Elves is $s1.
	THE_CURRENT_SCORE_FOR_THE_DARK_ELVES_IS_S1(907),
	// Message: The current score for the Orcs is $s1.
	THE_CURRENT_SCORE_FOR_THE_ORCS_IS_S1(908),
	// Message: The current score for the Dwarves is $s1.
	THE_CURRENT_SCORE_FOR_THE_DWARVES_IS_S1(909),
	// Message: Current location : $s1, $s2, $s3 (Near Talking Island Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_TALKING_ISLAND_VILLAGE(910),
	// Message: Current location : $s1, $s2, $s3 (Near Gludin Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_GLUDIN_VILLAGE(911),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Gludio)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_GLUDIO(912),
	// Message: Current location : $s1, $s2, $s3 (Near the Neutral Zone)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_NEUTRAL_ZONE(913),
	// Message: Current location : $s1, $s2, $s3 (Near the Elven Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_ELVEN_VILLAGE(914),
	// Message: Current location : $s1, $s2, $s3 (Near the Dark Elf Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_DARK_ELF_VILLAGE(915),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Dion)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_DION(916),
	// Message: Current location : $s1, $s2, $s3 (Near the Floran Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_FLORAN_VILLAGE(917),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Giran)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_GIRAN(918),
	// Message: Current location : $s1, $s2, $s3 (Near Giran Harbor)
	CURRENT_LOCATION__S1_S2_S3_NEAR_GIRAN_HARBOR(919),
	// Message: Current location : $s1, $s2, $s3 (Near the Orc Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_ORC_VILLAGE(920),
	// Message: Current location : $s1, $s2, $s3 (Near the Dwarven Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_DWARVEN_VILLAGE(921),
	// Message: Current location : $s1, $s2, $s3 (Near the Town of Oren)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_TOWN_OF_OREN(922),
	// Message: Current location : $s1, $s2, $s3 (Near Hunters Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_HUNTERS_VILLAGE(923),
	// Message: Current location : $s1, $s2, $s3 (Near Aden Castle Town)
	CURRENT_LOCATION__S1_S2_S3_NEAR_ADEN_CASTLE_TOWN(924),
	// Message: Current location : $s1, $s2, $s3 (Near the Coliseum)
	CURRENT_LOCATION__S1_S2_S3_NEAR_THE_COLISEUM(925),
	// Message: Current location : $s1, $s2, $s3 (Near Heine)
	CURRENT_LOCATION__S1_S2_S3_NEAR_HEINE(926),
	// Message: The current time is $s1:$s2 in the day.
	THE_CURRENT_TIME_IS_S1S2_IN_THE_DAY(927),
	// Message: The current time is $s1:$s2 in the night.
	THE_CURRENT_TIME_IS_S1S2_IN_THE_NIGHT(928),
	// Message: No compensation was given for the farm products.
	NO_COMPENSATION_WAS_GIVEN_FOR_THE_FARM_PRODUCTS(929),
	// Message: Lottery tickets are not currently being sold.
	LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD(930),
	// Message: The winning lottery ticket number has not yet been announced.
	THE_WINNING_LOTTERY_TICKET_NUMBER_HAS_NOT_YET_BEEN_ANNOUNCED(931),
	// Message: You cannot chat locally while observing.
	YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING(932),
	// Message: The seed pricing greatly differs from standard seed prices.
	THE_SEED_PRICING_GREATLY_DIFFERS_FROM_STANDARD_SEED_PRICES(933),
	// Message: It is a deleted recipe.
	IT_IS_A_DELETED_RECIPE(934),
	// Message: The amount is not sufficient and so the manor is not in operation.
	THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION(935),
	// Message: Use $s1.
	USE_S1(936),
	// Message: Currently preparing for private workshop.
	CURRENTLY_PREPARING_FOR_PRIVATE_WORKSHOP(937),
	// Message: The community server is currently offline.
	THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE(938),
	// Message: You cannot exchange while blocking everything.
	YOU_CANNOT_EXCHANGE_WHILE_BLOCKING_EVERYTHING(939),
	// Message: $s1 is blocking everything.
	S1_IS_BLOCKING_EVERYTHING(940),
	// Message: Restart at Talking Island Village.
	RESTART_AT_TALKING_ISLAND_VILLAGE(941),
	// Message: Restart at Gludin Village.
	RESTART_AT_GLUDIN_VILLAGE(942),
	// Message: Restart at the Town of Gludin.
	RESTART_AT_THE_TOWN_OF_GLUDIN(943),
	// Message: Restart at the Neutral Zone.
	RESTART_AT_THE_NEUTRAL_ZONE(944),
	// Message: Restart at the Elven Village.
	RESTART_AT_THE_ELVEN_VILLAGE(945),
	// Message: Restart at the Dark Elf Village.
	RESTART_AT_THE_DARK_ELF_VILLAGE(946),
	// Message: Restart at the Town of Dion.
	RESTART_AT_THE_TOWN_OF_DION(947),
	// Message: Restart at Floran Village.
	RESTART_AT_FLORAN_VILLAGE(948),
	// Message: Restart at the Town of Giran.
	RESTART_AT_THE_TOWN_OF_GIRAN(949),
	// Message: Restart at Giran Harbor.
	RESTART_AT_GIRAN_HARBOR(950),
	// Message: Restart at the Orc Village.
	RESTART_AT_THE_ORC_VILLAGE(951),
	// Message: Restart at the Dwarven Village.
	RESTART_AT_THE_DWARVEN_VILLAGE(952),
	// Message: Restart at the Town of Oren.
	RESTART_AT_THE_TOWN_OF_OREN(953),
	// Message: Restart at Hunters Village.
	RESTART_AT_HUNTERS_VILLAGE(954),
	// Message: Restart at the Town of Aden.
	RESTART_AT_THE_TOWN_OF_ADEN(955),
	// Message: Restart at the Coliseum.
	RESTART_AT_THE_COLISEUM(956),
	// Message: Restart at Heine.
	RESTART_AT_HEINE(957),
	// Message: Items cannot be discarded or destroyed while operating a private store or workshop.
	ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP(958),
	// Message: $s1 (*$s2) manufactured successfully.
	S1_S2_MANUFACTURED_SUCCESSFULLY(959),
	// Message: $s1 manufacturing failure.
	S1_MANUFACTURING_FAILURE(960),
	// Message: You are now blocking everything.
	YOU_ARE_NOW_BLOCKING_EVERYTHING(961),
	// Message: You are no longer blocking everything.
	YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING(962),
	// Message: Please determine the manufacturing price.
	PLEASE_DETERMINE_THE_MANUFACTURING_PRICE(963),
	// Message: Chatting is prohibited for one minute.
	CHATTING_IS_PROHIBITED_FOR_ONE_MINUTE(964),
	// Message: The chatting prohibition has been removed.
	THE_CHATTING_PROHIBITION_HAS_BEEN_REMOVED(965),
	// Message: Chatting is currently prohibited. If you try to chat before the prohibition is removed, the prohibition time will become even longer.
	CHATTING_IS_CURRENTLY_PROHIBITED_IF_YOU_TRY_TO_CHAT_BEFORE_THE_PROHIBITION_IS_REMOVED_THE_PROHIBITION_TIME_WILL_BECOME_EVEN_LONGER(966),
	// Message: Do you accept the party invitation from $s1? (Item distribution: Random including spoil)
	DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_RANDOM_INCLUDING_SPOIL(967),
	// Message: Do you accept the party invitation from $s1? (Item distribution: By turn)
	DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN(968),
	// Message: Do you accept the party invitation from $s1? (Item distribution: By turn including spoil)
	DO_YOU_ACCEPT_THE_PARTY_INVITATION_FROM_S1_ITEM_DISTRIBUTION_BY_TURN_INCLUDING_SPOIL(969),
	// Message: $s2's MP has been drained by $s1.
	S2S_MP_HAS_BEEN_DRAINED_BY_S1(970),
	// Message: Petitions cannot exceed 255 characters.
	PETITIONS_CANNOT_EXCEED_255_CHARACTERS(971),
	// Message: Pets cannot use this item.
	PETS_CANNOT_USE_THIS_ITEM(972),
	// Message: Please input no more than the number you have.
	PLEASE_INPUT_NO_MORE_THAN_THE_NUMBER_YOU_HAVE(973),
	// Message: The soul crystal succeeded in absorbing a soul.
	THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL(974),
	// Message: The soul crystal was not able to absorb a soul.
	THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_A_SOUL(975),
	// Message: The soul crystal broke because it was not able to endure the soul energy.
	THE_SOUL_CRYSTAL_BROKE_BECAUSE_IT_WAS_NOT_ABLE_TO_ENDURE_THE_SOUL_ENERGY(976),
	// Message: The soul crystals caused resonation and failed at absorbing a soul.
	THE_SOUL_CRYSTALS_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL(977),
	// Message: The soul crystal is refusing to absorb a soul.
	THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_A_SOUL(978),
	// Message: The ferry arrived at Talking Island Harbor.
	THE_FERRY_ARRIVED_AT_TALKING_ISLAND_HARBOR(979),
	// Message: The ferry will leave for Gludin Harbor after anchoring for ten minutes.
	THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES(980),
	// Message: The ferry will leave for Gludin Harbor in five minutes.
	THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_FIVE_MINUTES(981),
	// Message: The ferry will leave for Gludin Harbor in one minute.
	THE_FERRY_WILL_LEAVE_FOR_GLUDIN_HARBOR_IN_ONE_MINUTE(982),
	// Message: Those wishing to ride should make haste to get on.
	THOSE_WISHING_TO_RIDE_SHOULD_MAKE_HASTE_TO_GET_ON(983),
	// Message: The ferry will be leaving soon for Gludin Harbor.
	THE_FERRY_WILL_BE_LEAVING_SOON_FOR_GLUDIN_HARBOR(984),
	// Message: The ferry is leaving for Gludin Harbor.
	THE_FERRY_IS_LEAVING_FOR_GLUDIN_HARBOR(985),
	// Message: The ferry has arrived at Gludin Harbor.
	THE_FERRY_HAS_ARRIVED_AT_GLUDIN_HARBOR(986),
	// Message: The ferry will leave for Talking Island Harbor after anchoring for ten minutes.
	THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES(987),
	// Message: The ferry will leave for Talking Island Harbor in five minutes.
	THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_FIVE_MINUTES(988),
	// Message: The ferry will leave for Talking Island Harbor in one minute.
	THE_FERRY_WILL_LEAVE_FOR_TALKING_ISLAND_HARBOR_IN_ONE_MINUTE(989),
	// Message: The ferry will be leaving soon for Talking Island Harbor.
	THE_FERRY_WILL_BE_LEAVING_SOON_FOR_TALKING_ISLAND_HARBOR(990),
	// Message: The ferry is leaving for Talking Island Harbor.
	THE_FERRY_IS_LEAVING_FOR_TALKING_ISLAND_HARBOR(991),
	// Message: The ferry has arrived at Giran Harbor.
	THE_FERRY_HAS_ARRIVED_AT_GIRAN_HARBOR(992),
	// Message: The ferry will leave for Giran Harbor after anchoring for ten minutes.
	THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_AFTER_ANCHORING_FOR_TEN_MINUTES(993),
	// Message: The ferry will leave for Giran Harbor in five minutes.
	THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_IN_FIVE_MINUTES(994),
	// Message: The ferry will leave for Giran Harbor in one minute.
	THE_FERRY_WILL_LEAVE_FOR_GIRAN_HARBOR_IN_ONE_MINUTE(995),
	// Message: The ferry will be leaving soon for Giran Harbor.
	THE_FERRY_WILL_BE_LEAVING_SOON_FOR_GIRAN_HARBOR(996),
	// Message: The ferry is leaving for Giran Harbor.
	THE_FERRY_IS_LEAVING_FOR_GIRAN_HARBOR(997),
	// Message: The Innadril pleasure boat has arrived. It will anchor for ten minutes.
	THE_INNADRIL_PLEASURE_BOAT_HAS_ARRIVED_IT_WILL_ANCHOR_FOR_TEN_MINUTES(998),
	// Message: The Innadril pleasure boat will leave in five minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_FIVE_MINUTES(999),
	// Message: The Innadril pleasure boat will leave in one minute.
	THE_INNADRIL_PLEASURE_BOAT_WILL_LEAVE_IN_ONE_MINUTE(1000),
	// Message: The Innadril pleasure boat will be leaving soon.
	THE_INNADRIL_PLEASURE_BOAT_WILL_BE_LEAVING_SOON(1001),
	// Message: The Innadril pleasure boat is leaving.
	THE_INNADRIL_PLEASURE_BOAT_IS_LEAVING(1002),
	// Message: Cannot process a monster race ticket.
	CANNOT_PROCESS_A_MONSTER_RACE_TICKET(1003),
	// Message: You have registered for a clan hall auction.
	YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION(1004),
	// Message: There is not enough adena in the clan hall warehouse.
	THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE(1005),
	// Message: You have bid in a clan hall auction.
	YOU_HAVE_BID_IN_A_CLAN_HALL_AUCTION(1006),
	// Message: The preliminary match registration of $s1 has finished.
	THE_PRELIMINARY_MATCH_REGISTRATION_OF_S1_HAS_FINISHED(1007),
	// Message: A hungry strider cannot be mounted or dismounted.
	A_HUNGRY_STRIDER_CANNOT_BE_MOUNTED_OR_DISMOUNTED(1008),
	// Message: A strider cannot be ridden when dead.
	A_STRIDER_CANNOT_BE_RIDDEN_WHEN_DEAD(1009),
	// Message: A dead strider cannot be ridden.
	A_DEAD_STRIDER_CANNOT_BE_RIDDEN(1010),
	// Message: A strider in battle cannot be ridden.
	A_STRIDER_IN_BATTLE_CANNOT_BE_RIDDEN(1011),
	// Message: A strider cannot be ridden while in battle.
	A_STRIDER_CANNOT_BE_RIDDEN_WHILE_IN_BATTLE(1012),
	// Message: A strider can be ridden only when standing.
	A_STRIDER_CAN_BE_RIDDEN_ONLY_WHEN_STANDING(1013),
	// Message: Your pet gained $s1 experience points.
	YOUR_PET_GAINED_S1_EXPERIENCE_POINTS(1014),
	// Message: Your pet hit for $s1 damage.
	YOUR_PET_HIT_FOR_S1_DAMAGE(1015),
	// Message: Your pet received $s2 damage caused by $s1.
	YOUR_PET_RECEIVED_S2_DAMAGE_CAUSED_BY_S1(1016),
	// Message: Pet's critical hit!
	PETS_CRITICAL_HIT(1017),
	// Message: Your pet uses $s1.
	YOUR_PET_USES_S1(1018),
	// Message: Your pet uses $s1.
	YOUR_PET_USES_S1_(1019),
	// Message: Your pet picked up $s1.
	YOUR_PET_PICKED_UP_S1(1020),
	// Message: Your pet picked up $s2 $s1(s).
	YOUR_PET_PICKED_UP_S2_S1S(1021),
	// Message: Your pet picked up +$s1 $s2.
	YOUR_PET_PICKED_UP_S1_S2(1022),
	// Message: Your pet picked up $s1 adena.
	YOUR_PET_PICKED_UP_S1_ADENA(1023),
	// Message: Your pet put on $s1.
	YOUR_PET_PUT_ON_S1(1024),
	// Message: Your pet took off $s1.
	YOUR_PET_TOOK_OFF_S1(1025),
	// Message: The summoned monster gave damage of $s1.
	THE_SUMMONED_MONSTER_GAVE_DAMAGE_OF_S1(1026),
	// Message: The summoned monster received damage of $s2 caused by $s1.
	THE_SUMMONED_MONSTER_RECEIVED_DAMAGE_OF_S2_CAUSED_BY_S1(1027),
	// Message: Summoned monster's critical hit!
	SUMMONED_MONSTERS_CRITICAL_HIT(1028),
	// Message: A summoned monster uses $s1.
	A_SUMMONED_MONSTER_USES_S1(1029),
	// Message: <Party Information>
	PARTY_INFORMATION(1030),
	// Message: Looting method: Finders keepers
	LOOTING_METHOD_FINDERS_KEEPERS(1031),
	// Message: Looting method: Random
	LOOTING_METHOD_RANDOM(1032),
	// Message: Looting method: Random including spoil
	LOOTING_METHOD_RANDOM_INCLUDING_SPOIL(1033),
	// Message: Looting method: By turn
	LOOTING_METHOD_BY_TURN(1034),
	// Message: Looting method: By turn including spoil
	LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL(1035),
	// Message: You have exceeded the quantity that can be inputted.
	YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED(1036),
	// Message: $s1 manufactured $s2.
	S1_MANUFACTURED_S2(1037),
	// Message: $s1 manufactured $s3 $s2(s).
	S1_MANUFACTURED_S3_S2S(1038),
	// Message: Items left at the clan hall warehouse can only be retrieved by the clan leader. Do you want to continue?
	ITEMS_LEFT_AT_THE_CLAN_HALL_WAREHOUSE_CAN_ONLY_BE_RETRIEVED_BY_THE_CLAN_LEADER_DO_YOU_WANT_TO_CONTINUE(1039),
	// Message: Packages sent by freight can be picked up from any Warehouse location.  Would you like to continue?
	PACKAGES_SENT_BY_FREIGHT_CAN_BE_PICKED_UP_FROM_ANY_WAREHOUSE_LOCATION__WOULD_YOU_LIKE_TO_CONTINUE(1040),
	// Message: The next seed purchase price is $s1 adena.
	THE_NEXT_SEED_PURCHASE_PRICE_IS_S1_ADENA(1041),
	// Message: The next farm goods purchase price is $s1 adena.
	THE_NEXT_FARM_GOODS_PURCHASE_PRICE_IS_S1_ADENA(1042),
	// Message: At the current time, the "/unstuck" command cannot be used. Please send in a petition.
	AT_THE_CURRENT_TIME_THE_UNSTUCK_COMMAND_CANNOT_BE_USED_PLEASE_SEND_IN_A_PETITION(1043),
	// Message: Monster race payout information is not available while tickets are being sold.
	MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD(1044),
	// Message: Currently, a monster race is not being set up.
	CURRENTLY_A_MONSTER_RACE_IS_NOT_BEING_SET_UP(1045),
	// Message: Monster race tickets are no longer available.
	MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE(1046),
	// Message: We did not succeed in producing $s1 item.
	WE_DID_NOT_SUCCEED_IN_PRODUCING_S1_ITEM(1047),
	// Message: While 'blocking' everything, whispering is not possible.
	WHILE_BLOCKING_EVERYTHING_WHISPERING_IS_NOT_POSSIBLE(1048),
	// Message: While 'blocking' everything, it is not possible to send invitations for organizing parties.
	WHILE_BLOCKING_EVERYTHING_IT_IS_NOT_POSSIBLE_TO_SEND_INVITATIONS_FOR_ORGANIZING_PARTIES(1049),
	// Message: There are no communities in my clan. Clan communities are allowed for clans with skill levels of 2 and higher.
	THERE_ARE_NO_COMMUNITIES_IN_MY_CLAN_CLAN_COMMUNITIES_ARE_ALLOWED_FOR_CLANS_WITH_SKILL_LEVELS_OF_2_AND_HIGHER(1050),
	// Message: Payment for your clan hall has not been made. Please make payment to your clan warehouse by $s1 tomorrow.
	PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW(1051),
	// Message: The clan hall fee is one week overdue; therefore the clan hall ownership has been revoked.
	THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED(1052),
	// Message: It is not possible to resurrect in battlefields where a siege war is taking place.
	IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE(1053),
	// Message: You have entered a mystical land.
	YOU_HAVE_ENTERED_A_MYSTICAL_LAND(1054),
	// Message: You have left a mystical land.
	YOU_HAVE_LEFT_A_MYSTICAL_LAND(1055),
	// Message: You have exceeded the storage capacity of the castle's vault.
	YOU_HAVE_EXCEEDED_THE_STORAGE_CAPACITY_OF_THE_CASTLES_VAULT(1056),
	// Message: This command can only be used in the relax server.
	THIS_COMMAND_CAN_ONLY_BE_USED_IN_THE_RELAX_SERVER(1057),
	// Message: The sales price for seeds is $s1 adena.
	THE_SALES_PRICE_FOR_SEEDS_IS_S1_ADENA(1058),
	// Message: The remaining purchasing amount is $s1 adena.
	THE_REMAINING_PURCHASING_AMOUNT_IS_S1_ADENA(1059),
	// Message: The remainder after selling the seeds is $s1.
	THE_REMAINDER_AFTER_SELLING_THE_SEEDS_IS_S1(1060),
	// Message: The recipe cannot be registered.  You do not have the ability to create items.
	THE_RECIPE_CANNOT_BE_REGISTERED__YOU_DO_NOT_HAVE_THE_ABILITY_TO_CREATE_ITEMS(1061),
	// Message: Writing something new is possible after level 10.
	WRITING_SOMETHING_NEW_IS_POSSIBLE_AFTER_LEVEL_10(1062),
	// Message: The Petition Service is currently unavailable, please try again later; in the interim, if you become trapped or unable to move, please use the '/unstuck' command.
	THE_PETITION_SERVICE_IS_CURRENTLY_UNAVAILABLE_PLEASE_TRY_AGAIN_LATER_IN_THE_INTERIM_IF_YOU_BECOME_TRAPPED_OR_UNABLE_TO_MOVE_PLEASE_USE_THE_UNSTUCK_COMMAND(1063),
	// Message: The equipment, +$s1 $s2, has been removed.
	THE_EQUIPMENT_S1_S2_HAS_BEEN_REMOVED(1064),
	// Message: While operating a private store or workshop, you cannot discard, destroy, or trade an item.
	WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM(1065),
	// Message: $s1 HP has been restored.
	S1_HP_HAS_BEEN_RESTORED(1066),
	// Message: $s2 HP has been restored by $s1.
	S2_HP_HAS_BEEN_RESTORED_BY_S1(1067),
	// Message: $s1 MP has been restored.
	S1_MP_HAS_BEEN_RESTORED(1068),
	// Message: $s2 MP has been restored by $s1.
	S2_MP_HAS_BEEN_RESTORED_BY_S1(1069),
	// Message: You do not have 'read' permission.
	YOU_DO_NOT_HAVE_READ_PERMISSION(1070),
	// Message: You do not have 'write' permission.
	YOU_DO_NOT_HAVE_WRITE_PERMISSION(1071),
	// Message: You have obtained a ticket for the Monster Race #$s1  -  Single.
	YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1____SINGLE(1072),
	// Message: You have obtained a ticket for the Monster Race #$s1  -  Single.
	YOU_HAVE_OBTAINED_A_TICKET_FOR_THE_MONSTER_RACE_S1____SINGLE_(1073),
	// Message: You do not meet the age requirement to purchase a Monster Race Ticket.
	YOU_DO_NOT_MEET_THE_AGE_REQUIREMENT_TO_PURCHASE_A_MONSTER_RACE_TICKET(1074),
	// Message: The bid amount must be higher than the previous bid.
	THE_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_PREVIOUS_BID(1075),
	// Message: The game cannot be terminated at this time.
	THE_GAME_CANNOT_BE_TERMINATED_AT_THIS_TIME(1076),
	// Message: A GameGuard Execution error has occurred.  Please send the *.erl file(s) located in the GameGuard folder to game@inca.co.kr.
	A_GAMEGUARD_EXECUTION_ERROR_HAS_OCCURRED__PLEASE_SEND_THE_ERL_FILES_LOCATED_IN_THE_GAMEGUARD_FOLDER_TO_GAMEINCACOKR(1077),
	// Message: When a user's keyboard input exceeds a certain cumulative score a chat ban will be applied. This is done to discourage spamming. Please avoid posting the same message multiple times during a short period.
	WHEN_A_USERS_KEYBOARD_INPUT_EXCEEDS_A_CERTAIN_CUMULATIVE_SCORE_A_CHAT_BAN_WILL_BE_APPLIED_THIS_IS_DONE_TO_DISCOURAGE_SPAMMING_PLEASE_AVOID_POSTING_THE_SAME_MESSAGE_MULTIPLE_TIMES_DURING_A_SHORT_PERIOD(1078),
	// Message:  The target is currently banned from chatting.
	THE_TARGET_IS_CURRENTLY_BANNED_FROM_CHATTING(1079),
	// Message: Being permanent, are you sure you wish to use the facelift potion - Type A?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_FACELIFT_POTION__TYPE_A(1080),
	// Message: Being permanent, are you sure you wish to use the hair dye potion - Type A?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION__TYPE_A(1081),
	// Message: Do you wish to use the hair style change potion – Type A? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_A_IT_IS_PERMANENT(1082),
	// Message: Facelift potion - Type A is being applied.
	FACELIFT_POTION__TYPE_A_IS_BEING_APPLIED(1083),
	// Message: Hair dye potion - Type A is being applied.
	HAIR_DYE_POTION__TYPE_A_IS_BEING_APPLIED(1084),
	// Message: The hair style change potion - Type A is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_A_IS_BEING_USED(1085),
	// Message: Your facial appearance has been changed.
	YOUR_FACIAL_APPEARANCE_HAS_BEEN_CHANGED(1086),
	// Message: Your hair color has been changed.
	YOUR_HAIR_COLOR_HAS_BEEN_CHANGED(1087),
	// Message: Your hair style has been changed.
	YOUR_HAIR_STYLE_HAS_BEEN_CHANGED(1088),
	// Message: $s1 has obtained a first anniversary commemorative item.
	S1_HAS_OBTAINED_A_FIRST_ANNIVERSARY_COMMEMORATIVE_ITEM(1089),
	// Message: Being permanent, are you sure you wish to use the facelift potion - Type B?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_FACELIFT_POTION__TYPE_B(1090),
	// Message: Being permanent, are you sure you wish to use the facelift potion - Type C?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_FACELIFT_POTION__TYPE_C(1091),
	// Message: Being permanent, are you sure you wish to use the hair dye potion - Type B?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION__TYPE_B(1092),
	// Message: Being permanent, are you sure you wish to use the hair dye potion - Type C?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION__TYPE_C(1093),
	// Message: Being permanent, are you sure you wish to use the hair dye potion - Type D?
	BEING_PERMANENT_ARE_YOU_SURE_YOU_WISH_TO_USE_THE_HAIR_DYE_POTION__TYPE_D(1094),
	// Message:  Do you wish to use the hair style change potion – Type B? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_B_IT_IS_PERMANENT(1095),
	// Message:  Do you wish to use the hair style change potion – Type C? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_C_IT_IS_PERMANENT(1096),
	// Message:  Do you wish to use the hair style change potion – Type D? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_D_IT_IS_PERMANENT(1097),
	// Message:  Do you wish to use the hair style change potion – Type E? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_E_IT_IS_PERMANENT(1098),
	// Message:  Do you wish to use the hair style change potion – Type F? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_F_IT_IS_PERMANENT(1099),
	// Message:  Do you wish to use the hair style change potion – Type G? It is permanent.
	DO_YOU_WISH_TO_USE_THE_HAIR_STYLE_CHANGE_POTION__TYPE_G_IT_IS_PERMANENT(1100),
	// Message: Facelift potion - Type B is being applied.
	FACELIFT_POTION__TYPE_B_IS_BEING_APPLIED(1101),
	// Message: Facelift potion - Type C is being applied.
	FACELIFT_POTION__TYPE_C_IS_BEING_APPLIED(1102),
	// Message: Hair dye potion - Type B is being applied.
	HAIR_DYE_POTION__TYPE_B_IS_BEING_APPLIED(1103),
	// Message: Hair dye potion - Type C is being applied.
	HAIR_DYE_POTION__TYPE_C_IS_BEING_APPLIED(1104),
	// Message: Hair dye potion - Type D is being applied.
	HAIR_DYE_POTION__TYPE_D_IS_BEING_APPLIED(1105),
	// Message: The hair style change potion - Type B is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_B_IS_BEING_USED(1106),
	// Message:  The hair style change potion - Type C is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_C_IS_BEING_USED(1107),
	// Message:  The hair style change potion - Type D is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_D_IS_BEING_USED(1108),
	// Message:  The hair style change potion - Type E is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_E_IS_BEING_USED(1109),
	// Message:  The hair style change potion - Type F is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_F_IS_BEING_USED(1110),
	// Message:  The hair style change potion - Type G is being used.
	THE_HAIR_STYLE_CHANGE_POTION__TYPE_G_IS_BEING_USED(1111),
	// Message: The prize amount for the winner of Lottery #$s1 is $s2 adena.  We have $s3 first prize winners.
	THE_PRIZE_AMOUNT_FOR_THE_WINNER_OF_LOTTERY_S1_IS_S2_ADENA__WE_HAVE_S3_FIRST_PRIZE_WINNERS(1112),
	// Message: The prize amount for Lucky Lottery #$s1  is $s2 adena. There was no first prize winner in this drawing, therefore the jackpot will be added to the next drawing.
	THE_PRIZE_AMOUNT_FOR_LUCKY_LOTTERY_S1__IS_S2_ADENA_THERE_WAS_NO_FIRST_PRIZE_WINNER_IN_THIS_DRAWING_THEREFORE_THE_JACKPOT_WILL_BE_ADDED_TO_THE_NEXT_DRAWING(1113),
	// Message: Your clan may not register to participate in a siege while under a grace period of the clan's dissolution.
	YOUR_CLAN_MAY_NOT_REGISTER_TO_PARTICIPATE_IN_A_SIEGE_WHILE_UNDER_A_GRACE_PERIOD_OF_THE_CLANS_DISSOLUTION(1114),
	// Message: Individuals may not surrender during combat.
	INDIVIDUALS_MAY_NOT_SURRENDER_DURING_COMBAT(1115),
	// Message: One cannot leave one's clan during combat.
	ONE_CANNOT_LEAVE_ONES_CLAN_DURING_COMBAT(1116),
	// Message: A clan member may not be dismissed during combat.
	A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT(1117),
	// Message: Progress in a quest is possible only when your inventory's weight and volume are less than 80 percent of capacity.
	PROGRESS_IN_A_QUEST_IS_POSSIBLE_ONLY_WHEN_YOUR_INVENTORYS_WEIGHT_AND_VOLUME_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY(1118),
	// Message: Quest was automatically canceled when you attempted to settle the accounts of your quest while your inventory exceeded 80 percent of capacity.
	QUEST_WAS_AUTOMATICALLY_CANCELED_WHEN_YOU_ATTEMPTED_TO_SETTLE_THE_ACCOUNTS_OF_YOUR_QUEST_WHILE_YOUR_INVENTORY_EXCEEDED_80_PERCENT_OF_CAPACITY(1119),
	// Message: You are still a member of the clan.
	YOU_ARE_STILL_A_MEMBER_OF_THE_CLAN(1120),
	// Message: You do not have the right to vote.
	YOU_DO_NOT_HAVE_THE_RIGHT_TO_VOTE(1121),
	// Message: There is no candidate.
	THERE_IS_NO_CANDIDATE(1122),
	// Message: Weight and volume limit has been exceeded. That skill is currently unavailable.
	WEIGHT_AND_VOLUME_LIMIT_HAS_BEEN_EXCEEDED_THAT_SKILL_IS_CURRENTLY_UNAVAILABLE(1123),
	// Message: A recipe book may not be used while using a skill.
	A_RECIPE_BOOK_MAY_NOT_BE_USED_WHILE_USING_A_SKILL(1124),
	// Message: An item may not be created while engaged in trading.
	AN_ITEM_MAY_NOT_BE_CREATED_WHILE_ENGAGED_IN_TRADING(1125),
	// Message: You cannot enter a negative number.
	YOU_CANNOT_ENTER_A_NEGATIVE_NUMBER(1126),
	// Message: The reward must be less than 10 times the standard price.
	THE_REWARD_MUST_BE_LESS_THAN_10_TIMES_THE_STANDARD_PRICE(1127),
	// Message: A private store may not be opened while using a skill.
	A_PRIVATE_STORE_MAY_NOT_BE_OPENED_WHILE_USING_A_SKILL(1128),
	// Message: This is not allowed while riding a ferry or boat.
	THIS_IS_NOT_ALLOWED_WHILE_RIDING_A_FERRY_OR_BOAT(1129),
	// Message: You have given $s1 damage to your target and $s2 damage to the servitor.
	YOU_HAVE_GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_THE_SERVITOR(1130),
	// Message: It is now midnight and the effect of $s1 can be felt.
	IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT(1131),
	// Message: It is dawn and the effect of $s1 will now disappear.
	IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR(1132),
	// Message: Since HP has decreased, the effect of $s1 can be felt.
	SINCE_HP_HAS_DECREASED_THE_EFFECT_OF_S1_CAN_BE_FELT(1133),
	// Message: Since HP has increased, the effect of $s1 will disappear.
	SINCE_HP_HAS_INCREASED_THE_EFFECT_OF_S1_WILL_DISAPPEAR(1134),
	// Message: While you are engaged in combat, you cannot operate a private store or private workshop.
	WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP(1135),
	// Message: Since there was an account that used this IP and attempted to log in illegally, this account is not allowed to connect to the game server for $s1 minutes. Please use another game server.
	SINCE_THERE_WAS_AN_ACCOUNT_THAT_USED_THIS_IP_AND_ATTEMPTED_TO_LOG_IN_ILLEGALLY_THIS_ACCOUNT_IS_NOT_ALLOWED_TO_CONNECT_TO_THE_GAME_SERVER_FOR_S1_MINUTES_PLEASE_USE_ANOTHER_GAME_SERVER(1136),
	// Message: $s1 harvested $s3 $s2(s).
	S1_HARVESTED_S3_S2S(1137),
	// Message: $s1 harvested $s2(s).
	S1_HARVESTED_S2S(1138),
	// Message: The weight and volume limit of your inventory must not be exceeded.
	THE_WEIGHT_AND_VOLUME_LIMIT_OF_YOUR_INVENTORY_MUST_NOT_BE_EXCEEDED(1139),
	// Message: Would you like to open the gate?
	WOULD_YOU_LIKE_TO_OPEN_THE_GATE(1140),
	// Message: Would you like to close the gate?
	WOULD_YOU_LIKE_TO_CLOSE_THE_GATE(1141),
	// Message: Since $s1 already exists nearby, you cannot summon it again.
	SINCE_S1_ALREADY_EXISTS_NEARBY_YOU_CANNOT_SUMMON_IT_AGAIN(1142),
	// Message: Since you do not have enough items to maintain the servitor's stay, the servitor will disappear.
	SINCE_YOU_DO_NOT_HAVE_ENOUGH_ITEMS_TO_MAINTAIN_THE_SERVITORS_STAY_THE_SERVITOR_WILL_DISAPPEAR(1143),
	// Message: Currently, you don't have anybody to chat with in the game.
	CURRENTLY_YOU_DONT_HAVE_ANYBODY_TO_CHAT_WITH_IN_THE_GAME(1144),
	// Message: $s2 has been created for $s1 after the payment of $s3 adena is received.
	S2_HAS_BEEN_CREATED_FOR_S1_AFTER_THE_PAYMENT_OF_S3_ADENA_IS_RECEIVED(1145),
	// Message: $s1 created $s2 after receiving $s3 adena.
	S1_CREATED_S2_AFTER_RECEIVING_S3_ADENA(1146),
	// Message: $s2 $s3 have been created for $s1 at the price of $s4 adena.
	S2_S3_HAVE_BEEN_CREATED_FOR_S1_AT_THE_PRICE_OF_S4_ADENA(1147),
	// Message: $s1 created $s2 $s3 at the price of $s4 adena.
	S1_CREATED_S2_S3_AT_THE_PRICE_OF_S4_ADENA(1148),
	// Message: The attempt to create $s2 for $s1 at the price of $s3 adena has failed.
	THE_ATTEMPT_TO_CREATE_S2_FOR_S1_AT_THE_PRICE_OF_S3_ADENA_HAS_FAILED(1149),
	// Message: $s1 has failed to create $s2 at the price of $s3 adena.
	S1_HAS_FAILED_TO_CREATE_S2_AT_THE_PRICE_OF_S3_ADENA(1150),
	// Message: $s2 is sold to $s1 at the price of $s3 adena.
	S2_IS_SOLD_TO_S1_AT_THE_PRICE_OF_S3_ADENA(1151),
	// Message: $s2 $s3 have been sold to $s1 for $s4 adena.
	S2_S3_HAVE_BEEN_SOLD_TO_S1_FOR_S4_ADENA(1152),
	// Message: $s2 has been purchased from $s1 at the price of $s3 adena.
	S2_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S3_ADENA(1153),
	// Message: $s3 $s2 has been purchased from $s1 for $s4 adena.
	S3_S2_HAS_BEEN_PURCHASED_FROM_S1_FOR_S4_ADENA(1154),
	// Message: +$s2$s3 has been sold to $s1 at the price of $s4 adena.
	S2S3_HAS_BEEN_SOLD_TO_S1_AT_THE_PRICE_OF_S4_ADENA(1155),
	// Message: +$s2$s3 has been purchased from $s1 at the price of $s4 adena.
	S2S3_HAS_BEEN_PURCHASED_FROM_S1_AT_THE_PRICE_OF_S4_ADENA(1156),
	// Message: Trying on state lasts for only 5 seconds. When a character's state changes, it can be cancelled.
	TRYING_ON_STATE_LASTS_FOR_ONLY_5_SECONDS_WHEN_A_CHARACTERS_STATE_CHANGES_IT_CAN_BE_CANCELLED(1157),
	// Message: You cannot dismount from this elevation.
	YOU_CANNOT_DISMOUNT_FROM_THIS_ELEVATION(1158),
	// Message: The ferry from Talking Island will arrive at Gludin Harbor in approximately 10 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_ARRIVE_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES(1159),
	// Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 5 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES(1160),
	// Message: The ferry from Talking Island will be arriving at Gludin Harbor in approximately 1 minute.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE(1161),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 15 minutes.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_15_MINUTES(1162),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 10 minutes.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES(1163),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 5 minutes.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES(1164),
	// Message: The ferry from Giran Harbor will be arriving at Talking Island in approximately 1 minute.
	THE_FERRY_FROM_GIRAN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE(1165),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 20 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_20_MINUTES(1166),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 15 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_15_MINUTES(1167),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 10 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_10_MINUTES(1168),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 5 minutes.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_5_MINUTES(1169),
	// Message: The ferry from Talking Island will be arriving at Giran Harbor in approximately 1 minute.
	THE_FERRY_FROM_TALKING_ISLAND_WILL_BE_ARRIVING_AT_GIRAN_HARBOR_IN_APPROXIMATELY_1_MINUTE(1170),
	// Message: The Innadril pleasure boat will arrive in approximately 20 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_20_MINUTES(1171),
	// Message: The Innadril pleasure boat will arrive in approximately 15 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_15_MINUTES(1172),
	// Message: The Innadril pleasure boat will arrive in approximately 10 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_10_MINUTES(1173),
	// Message: The Innadril pleasure boat will arrive in approximately 5 minutes.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_5_MINUTES(1174),
	// Message: The Innadril pleasure boat will arrive in approximately 1 minute.
	THE_INNADRIL_PLEASURE_BOAT_WILL_ARRIVE_IN_APPROXIMATELY_1_MINUTE(1175),
	// Message: This is a quest event period.
	THIS_IS_A_QUEST_EVENT_PERIOD(1176),
	// Message: This is the seal validation period.
	THIS_IS_THE_SEAL_VALIDATION_PERIOD(1177),
	// Message: This seal permits the group that holds it to exclusively enter the dungeon opened by the Seal of Avarice during the seal validation period.  It also permits trading with the Blacksmith of Mammon who appears in special dungeons and permits meetings with Anakim or Lilith in the Disciple's Necropolis.
	THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_EXCLUSIVELY_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_AVARICE_DURING_THE_SEAL_VALIDATION_PERIOD__IT_ALSO_PERMITS_TRADING_WITH_THE_BLACKSMITH_OF_MAMMON_WHO_APPEARS_IN_SPECIAL_DUNGEONS_AND_PERMITS_MEETINGS_WITH_ANAKIM_OR_LILITH_IN_THE_DISCIPLES_NECROPOLIS(1178),
	// Message: This seal permits the group that holds it to enter the dungeon opened by the Seal of Gnosis, use the teleportation service offered by the priest in the village, and do business with the Blacksmith of Mammon. The Orator of Revelations appears and casts good magic on the winners, and the Preacher of Doom appears and casts bad magic on the losers.
	THIS_SEAL_PERMITS_THE_GROUP_THAT_HOLDS_IT_TO_ENTER_THE_DUNGEON_OPENED_BY_THE_SEAL_OF_GNOSIS_USE_THE_TELEPORTATION_SERVICE_OFFERED_BY_THE_PRIEST_IN_THE_VILLAGE_AND_DO_BUSINESS_WITH_THE_BLACKSMITH_OF_MAMMON_THE_ORATOR_OF_REVELATIONS_APPEARS_AND_CASTS_GOOD_MAGIC_ON_THE_WINNERS_AND_THE_PREACHER_OF_DOOM_APPEARS_AND_CASTS_BAD_MAGIC_ON_THE_LOSERS(1179),
	// Message: During the Seal Validation period, the cabal's maximum CP amount increases. In addition, the cabal posessing the seal will benefit from favorable changes in the cost to upgrade castle defense mercenaries, castle gates and walls; basic P. Def. of castle gates and walls; and the limit imposed on the castle tax rate. The use of siege war weapons will also be limited. If the Revolutionary Army of Dusk takes possession of this seal during the castle siege war, only the allies of the clan that owns the castle can come to the aid of the defenders.
	DURING_THE_SEAL_VALIDATION_PERIOD_THE_CABALS_MAXIMUM_CP_AMOUNT_INCREASES_IN_ADDITION_THE_CABAL_POSESSING_THE_SEAL_WILL_BENEFIT_FROM_FAVORABLE_CHANGES_IN_THE_COST_TO_UPGRADE_CASTLE_DEFENSE_MERCENARIES_CASTLE_GATES_AND_WALLS_BASIC_P_DEF_OF_CASTLE_GATES_AND_WALLS_AND_THE_LIMIT_IMPOSED_ON_THE_CASTLE_TAX_RATE_THE_USE_OF_SIEGE_WAR_WEAPONS_WILL_ALSO_BE_LIMITED_IF_THE_REVOLUTIONARY_ARMY_OF_DUSK_TAKES_POSSESSION_OF_THIS_SEAL_DURING_THE_CASTLE_SIEGE_WAR_ONLY_THE_ALLIES_OF_THE_CLAN_THAT_OWNS_THE_CASTLE_CAN_COME_TO_THE_AID_OF_THE_DEFENDERS(1180),
	// Message: Do you really wish to change the title?
	DO_YOU_REALLY_WISH_TO_CHANGE_THE_TITLE(1181),
	// Message: Are you sure you wish to delete the clan crest?
	ARE_YOU_SURE_YOU_WISH_TO_DELETE_THE_CLAN_CREST(1182),
	// Message: This is the initial period.
	THIS_IS_THE_INITIAL_PERIOD(1183),
	// Message: This is a period of calculating statistics in the server.
	THIS_IS_A_PERIOD_OF_CALCULATING_STATISTICS_IN_THE_SERVER(1184),
	// Message: days left until deletion.
	DAYS_LEFT_UNTIL_DELETION(1185),
	// Message: In order to open a new account, please visit the official Lineage II website (http://www.lineage2.com) and sign up under the 'My Account' tab.
	IN_ORDER_TO_OPEN_A_NEW_ACCOUNT_PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_WEBSITE_HTTPWWWLINEAGE2COM_AND_SIGN_UP_UNDER_THE_MY_ACCOUNT_TAB(1186),
	// Message: If you have lost your account information, please visit the official Lineage II support website at http://support.plaync.com
	IF_YOU_HAVE_LOST_YOUR_ACCOUNT_INFORMATION_PLEASE_VISIT_THE_OFFICIAL_LINEAGE_II_SUPPORT_WEBSITE_AT_HTTPSUPPORTPLAYNCCOM(1187),
	// Message: Your selected target can no longer receive a recommendation.
	YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION(1188),
	// Message: The temporary alliance of the Castle Attacker team is in effect. It will be dissolved when the Castle Lord is replaced.
	THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT_IT_WILL_BE_DISSOLVED_WHEN_THE_CASTLE_LORD_IS_REPLACED(1189),
	// Message: The temporary alliance of the Castle Attacker team has been dissolved.
	THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED(1190),
	// Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 10 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_10_MINUTES(1191),
	// Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 5 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_5_MINUTES(1192),
	// Message: The ferry from Gludin Harbor will be arriving at Talking Island in approximately 1 minute.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_TALKING_ISLAND_IN_APPROXIMATELY_1_MINUTE(1193),
	// Message: A mercenary can be assigned to a position from the beginning of the Seal Validation period until the time when a siege starts.
	A_MERCENARY_CAN_BE_ASSIGNED_TO_A_POSITION_FROM_THE_BEGINNING_OF_THE_SEAL_VALIDATION_PERIOD_UNTIL_THE_TIME_WHEN_A_SIEGE_STARTS(1194),
	// Message: This mercenary cannot be assigned to a position by using the Seal of Strife.
	THIS_MERCENARY_CANNOT_BE_ASSIGNED_TO_A_POSITION_BY_USING_THE_SEAL_OF_STRIFE(1195),
	// Message: Your force has reached maximum capacity.
	YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY_(1196),
	// Message: Summoning a servitor costs $s2 $s1.
	SUMMONING_A_SERVITOR_COSTS_S2_S1(1197),
	// Message: The item has been successfully crystallized.
	THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED(1198),
	// Message: =======<Clan War Target>=======
	CLAN_WAR_TARGET(1199),
	// Message: = $s1 ($s2 Alliance)
	_S1_S2_ALLIANCE(1200),
	// Message: Please select the quest you wish to abort.
	PLEASE_SELECT_THE_QUEST_YOU_WISH_TO_ABORT(1201),
	// Message: = $s1 (No alliance exists)
	_S1_NO_ALLIANCE_EXISTS(1202),
	// Message: There is no clan war in progress.
	THERE_IS_NO_CLAN_WAR_IN_PROGRESS(1203),
	// Message: The screenshot has been saved. ($s1 $s2x$s3)
	THE_SCREENSHOT_HAS_BEEN_SAVED_S1_S2XS3(1204),
	// Message: Your mailbox is full. There is a 100 message limit.
	YOUR_MAILBOX_IS_FULL_THERE_IS_A_100_MESSAGE_LIMIT(1205),
	// Message: The memo box is full.  There is a 100 memo limit.
	THE_MEMO_BOX_IS_FULL__THERE_IS_A_100_MEMO_LIMIT(1206),
	// Message: Please make an entry in the field.
	PLEASE_MAKE_AN_ENTRY_IN_THE_FIELD(1207),
	// Message: $s1 died and dropped $s3 $s2.
	S1_DIED_AND_DROPPED_S3_S2(1208),
	// Message: Congratulations. Your raid was successful.
	CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL(1209),
	// Message: Seven Signs:  The quest event period has begun.  Visit a Priest of Dawn or Priestess of Dusk to participate in the event.
	SEVEN_SIGNS__THE_QUEST_EVENT_PERIOD_HAS_BEGUN__VISIT_A_PRIEST_OF_DAWN_OR_PRIESTESS_OF_DUSK_TO_PARTICIPATE_IN_THE_EVENT(1210),
	// Message: Seven Signs: The quest event period has ended. The next quest event will start in one week.
	SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_ENDED_THE_NEXT_QUEST_EVENT_WILL_START_IN_ONE_WEEK(1211),
	// Message: Seven Signs: The Lords of Dawn have obtained the Seal of Avarice.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_AVARICE(1212),
	// Message: Seven Signs: The Lords of Dawn have obtained the Seal of Gnosis.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS(1213),
	// Message: Seven Signs: The Lords of Dawn have obtained the Seal of Strife.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_OBTAINED_THE_SEAL_OF_STRIFE(1214),
	// Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Avarice.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_AVARICE(1215),
	// Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Gnosis.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_GNOSIS(1216),
	// Message: Seven Signs: The Revolutionaries of Dusk have obtained the Seal of Strife.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_OBTAINED_THE_SEAL_OF_STRIFE(1217),
	// Message: Seven Signs: The Seal Validation period has begun.
	SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_BEGUN(1218),
	// Message: Seven Signs: The Seal Validation period has ended.
	SEVEN_SIGNS_THE_SEAL_VALIDATION_PERIOD_HAS_ENDED(1219),
	// Message: Are you sure you wish to summon it?
	ARE_YOU_SURE_YOU_WISH_TO_SUMMON_IT(1220),
	// Message: Do you really wish to return it?
	DO_YOU_REALLY_WISH_TO_RETURN_IT(1221),
	// Message: Current Location: $s1, $s2, $s3 (GM Consultation Service)
	CURRENT_LOCATION_S1_S2_S3_GM_CONSULTATION_SERVICE(1222),
	// Message: We depart for Talking Island in five minutes.
	WE_DEPART_FOR_TALKING_ISLAND_IN_FIVE_MINUTES(1223),
	// Message: We depart for Talking Island in one minute.
	WE_DEPART_FOR_TALKING_ISLAND_IN_ONE_MINUTE(1224),
	// Message: All aboard for Talking Island!
	ALL_ABOARD_FOR_TALKING_ISLAND(1225),
	// Message: We are now leaving for Talking Island.
	WE_ARE_NOW_LEAVING_FOR_TALKING_ISLAND(1226),
	// Message: You have $s1 unread messages.
	YOU_HAVE_S1_UNREAD_MESSAGES(1227),
	// Message: $s1 has blocked you. You cannot send mail to $s1 .
	S1_HAS_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL_TO_S1_(1228),
	// Message: No more messages may be sent at this time. Each account is allowed 10 messages per day.
	NO_MORE_MESSAGES_MAY_BE_SENT_AT_THIS_TIME_EACH_ACCOUNT_IS_ALLOWED_10_MESSAGES_PER_DAY(1229),
	// Message: You are limited to five recipients at a time.
	YOU_ARE_LIMITED_TO_FIVE_RECIPIENTS_AT_A_TIME(1230),
	// Message: You've sent mail.
	YOUVE_SENT_MAIL(1231),
	// Message: The message was not sent.
	THE_MESSAGE_WAS_NOT_SENT(1232),
	// Message: You've got mail.
	YOUVE_GOT_MAIL(1233),
	// Message: The mail has been stored in your temporary mailbox.
	THE_MAIL_HAS_BEEN_STORED_IN_YOUR_TEMPORARY_MAILBOX(1234),
	// Message: Do you wish to delete all your friends?
	DO_YOU_WISH_TO_DELETE_ALL_YOUR_FRIENDS(1235),
	// Message: Please enter security card number.
	PLEASE_ENTER_SECURITY_CARD_NUMBER(1236),
	// Message: Please enter the card number for number $s1.
	PLEASE_ENTER_THE_CARD_NUMBER_FOR_NUMBER_S1(1237),
	// Message: Your temporary mailbox is full.  No more mail can be stored; you have reached the 10 message limit.
	YOUR_TEMPORARY_MAILBOX_IS_FULL__NO_MORE_MAIL_CAN_BE_STORED_YOU_HAVE_REACHED_THE_10_MESSAGE_LIMIT(1238),
	// Message: The keyboard security module has failed to load. Please exit the game and try again.
	THE_KEYBOARD_SECURITY_MODULE_HAS_FAILED_TO_LOAD_PLEASE_EXIT_THE_GAME_AND_TRY_AGAIN(1239),
	// Message: Seven Signs: The Revolutionaries of Dusk have won.
	SEVEN_SIGNS_THE_REVOLUTIONARIES_OF_DUSK_HAVE_WON(1240),
	// Message: Seven Signs: The Lords of Dawn have won.
	SEVEN_SIGNS_THE_LORDS_OF_DAWN_HAVE_WON(1241),
	// Message: Users who have not verified their age may not log in between the hours of 10:00 p.m. and 6:00 a.m.
	USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_MAY_NOT_LOG_IN_BETWEEN_THE_HOURS_OF_1000_PM_AND_600_AM(1242),
	// Message: The security card number is invalid.
	THE_SECURITY_CARD_NUMBER_IS_INVALID(1243),
	// Message: Users who have not verified their age may not log in between the hours of 10:00 p.m. and 6:00 a.m. Logging off now.
	USERS_WHO_HAVE_NOT_VERIFIED_THEIR_AGE_MAY_NOT_LOG_IN_BETWEEN_THE_HOURS_OF_1000_PM_AND_600_AM_LOGGING_OFF_NOW(1244),
	// Message: You will be logged out in $s1 minutes.
	YOU_WILL_BE_LOGGED_OUT_IN_S1_MINUTES(1245),
	// Message: $s1 died and has dropped $s2 adena.
	S1_DIED_AND_HAS_DROPPED_S2_ADENA(1246),
	// Message: The corpse is too old. The skill cannot be used.
	THE_CORPSE_IS_TOO_OLD_THE_SKILL_CANNOT_BE_USED(1247),
	// Message: You are out of feed. Mount status canceled.
	YOU_ARE_OUT_OF_FEED_MOUNT_STATUS_CANCELED(1248),
	// Message: You may only ride a wyvern while you're riding a strider.
	YOU_MAY_ONLY_RIDE_A_WYVERN_WHILE_YOURE_RIDING_A_STRIDER(1249),
	// Message: Do you really want to surrender? If you surrender during an alliance war, your Exp will drop the same as if you were to die once.
	DO_YOU_REALLY_WANT_TO_SURRENDER_IF_YOU_SURRENDER_DURING_AN_ALLIANCE_WAR_YOUR_EXP_WILL_DROP_THE_SAME_AS_IF_YOU_WERE_TO_DIE_ONCE(1250),
	// Message: Are you sure you want to dismiss the alliance? If you use the /allydismiss command, you will not be able to accept another clan to your alliance for one day.
	ARE_YOU_SURE_YOU_WANT_TO_DISMISS_THE_ALLIANCE_IF_YOU_USE_THE_ALLYDISMISS_COMMAND_YOU_WILL_NOT_BE_ABLE_TO_ACCEPT_ANOTHER_CLAN_TO_YOUR_ALLIANCE_FOR_ONE_DAY(1251),
	// Message: Are you sure you want to surrender? Exp penalty will be the same as death.
	ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH(1252),
	// Message: Are you sure you want to surrender? Exp penalty will be the same as death and you will not be allowed to participate in clan war.
	ARE_YOU_SURE_YOU_WANT_TO_SURRENDER_EXP_PENALTY_WILL_BE_THE_SAME_AS_DEATH_AND_YOU_WILL_NOT_BE_ALLOWED_TO_PARTICIPATE_IN_CLAN_WAR(1253),
	// Message: Thank you for submitting feedback.
	THANK_YOU_FOR_SUBMITTING_FEEDBACK(1254),
	// Message: GM consultation has begun.
	GM_CONSULTATION_HAS_BEGUN(1255),
	// Message: Please write the name after the command.
	PLEASE_WRITE_THE_NAME_AFTER_THE_COMMAND(1256),
	// Message: The special skill of a servitor or pet cannot be registered as a macro.
	THE_SPECIAL_SKILL_OF_A_SERVITOR_OR_PET_CANNOT_BE_REGISTERED_AS_A_MACRO(1257),
	// Message: $s1 has been crystallized.
	S1_HAS_BEEN_CRYSTALLIZED(1258),
	// Message: =======<Alliance Target>=======
	ALLIANCE_TARGET(1259),
	// Message: Seven Signs: Preparations have begun for the next quest event.
	SEVEN_SIGNS_PREPARATIONS_HAVE_BEGUN_FOR_THE_NEXT_QUEST_EVENT(1260),
	// Message: Seven Signs: The quest event period has begun. Speak with a Priest of Dawn or Dusk Priestess if you wish to participate in the event.
	SEVEN_SIGNS_THE_QUEST_EVENT_PERIOD_HAS_BEGUN_SPEAK_WITH_A_PRIEST_OF_DAWN_OR_DUSK_PRIESTESS_IF_YOU_WISH_TO_PARTICIPATE_IN_THE_EVENT(1261),
	// Message: Seven Signs: Quest event has ended. Results are being tallied.
	SEVEN_SIGNS_QUEST_EVENT_HAS_ENDED_RESULTS_ARE_BEING_TALLIED(1262),
	// Message: Seven Signs: This is the seal validation period. A new quest event period begins next Monday.
	SEVEN_SIGNS_THIS_IS_THE_SEAL_VALIDATION_PERIOD_A_NEW_QUEST_EVENT_PERIOD_BEGINS_NEXT_MONDAY(1263),
	// Message: This soul stone cannot currently absorb souls. Absorption has failed.
	THIS_SOUL_STONE_CANNOT_CURRENTLY_ABSORB_SOULS_ABSORPTION_HAS_FAILED(1264),
	// Message: You can't absorb souls without a soul stone.
	YOU_CANT_ABSORB_SOULS_WITHOUT_A_SOUL_STONE(1265),
	// Message: The exchange has ended.
	THE_EXCHANGE_HAS_ENDED(1266),
	// Message: Your contribution score is increased by $s1.
	YOUR_CONTRIBUTION_SCORE_IS_INCREASED_BY_S1(1267),
	// Message: Do you wish to add $s1 class as your sub class?
	DO_YOU_WISH_TO_ADD_S1_CLASS_AS_YOUR_SUB_CLASS(1268),
	// Message: The new sub class has been added.
	THE_NEW_SUB_CLASS_HAS_BEEN_ADDED(1269),
	// Message: The transfer of sub class has been completed.
	THE_TRANSFER_OF_SUB_CLASS_HAS_BEEN_COMPLETED(1270),
	// Message: Do you wish to participate? Until the next seal validation period, you are a member of the Lords of Dawn.
	DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_LORDS_OF_DAWN(1271),
	// Message: Do you wish to participate? Until the next seal validation period, you are a member of the Revolutionaries of Dusk.
	DO_YOU_WISH_TO_PARTICIPATE_UNTIL_THE_NEXT_SEAL_VALIDATION_PERIOD_YOU_ARE_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK(1272),
	// Message: You will participate in the Seven Signs as a member of the Lords of Dawn.
	YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN(1273),
	// Message: You will participate in the Seven Signs as a member of the Revolutionaries of Dusk.
	YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK(1274),
	// Message: You've chosen to fight for the Seal of Avarice during this quest event period.
	YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD(1275),
	// Message: You've chosen to fight for the Seal of Gnosis during this quest event period.
	YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD(1276),
	// Message: You've chosen to fight for the Seal of Strife during this quest event period.
	YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD(1277),
	// Message: The NPC server is not operating at this time.
	THE_NPC_SERVER_IS_NOT_OPERATING_AT_THIS_TIME(1278),
	// Message: Contribution level has exceeded the limit. You may not continue.
	CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE(1279),
	// Message: Magic Critical Hit!
	MAGIC_CRITICAL_HIT(1280),
	// Message: Your excellent shield defense was a success!
	YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS(1281),
	// Message: Your Karma has been changed to $s1.
	YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1(1282),
	// Message: The minimum frame option has been activated.
	THE_MINIMUM_FRAME_OPTION_HAS_BEEN_ACTIVATED(1283),
	// Message: The minimum frame option has been deactivated.
	THE_MINIMUM_FRAME_OPTION_HAS_BEEN_DEACTIVATED(1284),
	// Message: No inventory exists. You cannot purchase an item.
	NO_INVENTORY_EXISTS_YOU_CANNOT_PURCHASE_AN_ITEM(1285),
	// Message: (Until next Monday at 6:00 p.m.)
	UNTIL_NEXT_MONDAY_AT_600_PM(1286),
	// Message: (Until today at 6:00 p.m.)
	UNTIL_TODAY_AT_600_PM(1287),
	// Message: If trends continue, $s1 will win and the seal will belong to:
	IF_TRENDS_CONTINUE_S1_WILL_WIN_AND_THE_SEAL_WILL_BELONG_TO(1288),
	// Message: Since the seal was owned during the previous period and 10 percent or more people have voted.
	SINCE_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_10_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED(1289),
	// Message: Although the seal was not owned, since 35 percent or more people have voted.
	ALTHOUGH_THE_SEAL_WAS_NOT_OWNED_SINCE_35_PERCENT_OR_MORE_PEOPLE_HAVE_VOTED(1290),
	// Message: Although the seal was owned during the previous period, because less than 10 percent of people have voted.
	ALTHOUGH_THE_SEAL_WAS_OWNED_DURING_THE_PREVIOUS_PERIOD_BECAUSE_LESS_THAN_10_PERCENT_OF_PEOPLE_HAVE_VOTED(1291),
	// Message: Since the seal was not owned during the previous period, and since less than 35 percent of people have voted.
	SINCE_THE_SEAL_WAS_NOT_OWNED_DURING_THE_PREVIOUS_PERIOD_AND_SINCE_LESS_THAN_35_PERCENT_OF_PEOPLE_HAVE_VOTED(1292),
	// Message: If current trends continue, it will end in a tie.
	IF_CURRENT_TRENDS_CONTINUE_IT_WILL_END_IN_A_TIE(1293),
	// Message: The competition has ended in a tie.  Therefore, nobody has been awarded the seal.
	THE_COMPETITION_HAS_ENDED_IN_A_TIE__THEREFORE_NOBODY_HAS_BEEN_AWARDED_THE_SEAL(1294),
	// Message: Sub classes may not be created or changed while a skill is in use.
	SUB_CLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE(1295),
	// Message: You cannot open a Private Store here.
	YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE(1296),
	// Message: You cannot open a Private Workshop here.
	YOU_CANNOT_OPEN_A_PRIVATE_WORKSHOP_HERE(1297),
	// Message: Please confirm that you would like to exit the Monster Race Track.
	PLEASE_CONFIRM_THAT_YOU_WOULD_LIKE_TO_EXIT_THE_MONSTER_RACE_TRACK(1298),
	// Message: $s1's casting has been interrupted.
	S1S_CASTING_HAS_BEEN_INTERRUPTED(1299),
	// Message: You are no longer trying on equipment.
	YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT(1300),
	// Message: Only a Lord of Dawn may use this.
	ONLY_A_LORD_OF_DAWN_MAY_USE_THIS(1301),
	// Message: Only a Revolutionary of Dusk may use this.
	ONLY_A_REVOLUTIONARY_OF_DUSK_MAY_USE_THIS(1302),
	// Message: This may only be used during the quest event period.
	THIS_MAY_ONLY_BE_USED_DURING_THE_QUEST_EVENT_PERIOD(1303),
	// Message: The influence of the Seal of Strife has caused all defensive registrations to be canceled, except for an Alliance with a castle owning clan.
	THE_INFLUENCE_OF_THE_SEAL_OF_STRIFE_HAS_CAUSED_ALL_DEFENSIVE_REGISTRATIONS_TO_BE_CANCELED_EXCEPT_FOR_AN_ALLIANCE_WITH_A_CASTLE_OWNING_CLAN(1304),
	// Message: Seal Stones may only be transferred during the quest event period.
	SEAL_STONES_MAY_ONLY_BE_TRANSFERRED_DURING_THE_QUEST_EVENT_PERIOD(1305),
	// Message: You are no longer trying on equipment.
	YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT_(1306),
	// Message: Only during the seal validation period may you settle your account.
	ONLY_DURING_THE_SEAL_VALIDATION_PERIOD_MAY_YOU_SETTLE_YOUR_ACCOUNT(1307),
	// Message: Congratulations - You've completed a class transfer!
	CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER(1308),
	// Message: To use this option, you must have the latest version of MSN Messenger installed on your computer.
	TO_USE_THIS_OPTION_YOU_MUST_HAVE_THE_LATEST_VERSION_OF_MSN_MESSENGER_INSTALLED_ON_YOUR_COMPUTER(1309),
	// Message: For full functionality, the latest version of MSN Messenger must be installed on your computer.
	FOR_FULL_FUNCTIONALITY_THE_LATEST_VERSION_OF_MSN_MESSENGER_MUST_BE_INSTALLED_ON_YOUR_COMPUTER(1310),
	// Message: Previous versions of MSN Messenger only provide the basic features for in-game MSN Messenger chat.  Add/Delete Contacts and other MSN Messenger options are not available.
	PREVIOUS_VERSIONS_OF_MSN_MESSENGER_ONLY_PROVIDE_THE_BASIC_FEATURES_FOR_INGAME_MSN_MESSENGER_CHAT__ADDDELETE_CONTACTS_AND_OTHER_MSN_MESSENGER_OPTIONS_ARE_NOT_AVAILABLE(1311),
	// Message: The latest version of MSN Messenger may be obtained from the MSN web site (http://messenger.msn.com).
	THE_LATEST_VERSION_OF_MSN_MESSENGER_MAY_BE_OBTAINED_FROM_THE_MSN_WEB_SITE_HTTPMESSENGERMSNCOM(1312),
	// Message: $s1, to better server our customers, all chat histories are stored and maintained by Ncsoft.  If you do not agree to have your chat records stored, please close the chat window now.  For more information regarding this procedure, please visit our home page at www.PlayNC.com.  Thank you!
	S1_TO_BETTER_SERVER_OUR_CUSTOMERS_ALL_CHAT_HISTORIES_ARE_STORED_AND_MAINTAINED_BY_NCSOFT__IF_YOU_DO_NOT_AGREE_TO_HAVE_YOUR_CHAT_RECORDS_STORED_PLEASE_CLOSE_THE_CHAT_WINDOW_NOW__FOR_MORE_INFORMATION_REGARDING_THIS_PROCEDURE_PLEASE_VISIT_OUR_HOME_PAGE_AT_WWWPLAYNCCOM__THANK_YOU(1313),
	// Message: Please enter the passport ID of the person you wish to add to your contact list.
	PLEASE_ENTER_THE_PASSPORT_ID_OF_THE_PERSON_YOU_WISH_TO_ADD_TO_YOUR_CONTACT_LIST(1314),
	// Message: Deleting a contact will remove that contact from MSN Messenger as well. The contact can still check your online status and will not be blocked from sending you a message.
	DELETING_A_CONTACT_WILL_REMOVE_THAT_CONTACT_FROM_MSN_MESSENGER_AS_WELL_THE_CONTACT_CAN_STILL_CHECK_YOUR_ONLINE_STATUS_AND_WILL_NOT_BE_BLOCKED_FROM_SENDING_YOU_A_MESSAGE(1315),
	// Message: The contact will be deleted and blocked from your contact list.
	THE_CONTACT_WILL_BE_DELETED_AND_BLOCKED_FROM_YOUR_CONTACT_LIST(1316),
	// Message: Would you like to delete this contact?
	WOULD_YOU_LIKE_TO_DELETE_THIS_CONTACT(1317),
	// Message: Please select the contact you want to block or unblock.
	PLEASE_SELECT_THE_CONTACT_YOU_WANT_TO_BLOCK_OR_UNBLOCK(1318),
	// Message: Please select the name of the contact you wish to change to another group.
	PLEASE_SELECT_THE_NAME_OF_THE_CONTACT_YOU_WISH_TO_CHANGE_TO_ANOTHER_GROUP(1319),
	// Message: After selecting the group you wish to move your contact to, press the OK button.
	AFTER_SELECTING_THE_GROUP_YOU_WISH_TO_MOVE_YOUR_CONTACT_TO_PRESS_THE_OK_BUTTON(1320),
	// Message: Enter the name of the group you wish to add.
	ENTER_THE_NAME_OF_THE_GROUP_YOU_WISH_TO_ADD(1321),
	// Message: Select the group and enter the new name.
	SELECT_THE_GROUP_AND_ENTER_THE_NEW_NAME(1322),
	// Message: Select the group you wish to delete and click the OK button.
	SELECT_THE_GROUP_YOU_WISH_TO_DELETE_AND_CLICK_THE_OK_BUTTON(1323),
	// Message: Signing in...
	SIGNING_IN(1324),
	// Message: You've logged into another computer and have been logged out of the .NET Messenger Service on this computer.
	YOUVE_LOGGED_INTO_ANOTHER_COMPUTER_AND_HAVE_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE_ON_THIS_COMPUTER(1325),
	// Message: $s1:
	S1(1326),
	// Message: The following message could not be delivered:
	THE_FOLLOWING_MESSAGE_COULD_NOT_BE_DELIVERED(1327),
	// Message: Members of the Revolutionaries of Dusk will not be resurrected.
	MEMBERS_OF_THE_REVOLUTIONARIES_OF_DUSK_WILL_NOT_BE_RESURRECTED(1328),
	// Message: You are currently blocked from using the Private Store and Private Workshop.
	YOU_ARE_CURRENTLY_BLOCKED_FROM_USING_THE_PRIVATE_STORE_AND_PRIVATE_WORKSHOP(1329),
	// Message: You may not open a Private Store or Private Workshop for another $s1 minute(s).
	YOU_MAY_NOT_OPEN_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_FOR_ANOTHER_S1_MINUTES(1330),
	// Message: You are no longer blocked from using the Private Store or Private Workshop.
	YOU_ARE_NO_LONGER_BLOCKED_FROM_USING_THE_PRIVATE_STORE_OR_PRIVATE_WORKSHOP(1331),
	// Message: Items may not be used after your character or pet dies.
	ITEMS_MAY_NOT_BE_USED_AFTER_YOUR_CHARACTER_OR_PET_DIES(1332),
	// Message: The replay file is not accessible.  Please verify that the replay.ini file exists in your Lineage 2 directory.
	THE_REPLAY_FILE_IS_NOT_ACCESSIBLE__PLEASE_VERIFY_THAT_THE_REPLAYINI_FILE_EXISTS_IN_YOUR_LINEAGE_2_DIRECTORY(1333),
	// Message: The new camera data has been stored.
	THE_NEW_CAMERA_DATA_HAS_BEEN_STORED(1334),
	// Message: The attempt to store the new camera data has failed.
	THE_ATTEMPT_TO_STORE_THE_NEW_CAMERA_DATA_HAS_FAILED(1335),
	// Message: The replay file, $s1.$s2 has been corrupted, please check the file.
	THE_REPLAY_FILE_S1S2_HAS_BEEN_CORRUPTED_PLEASE_CHECK_THE_FILE(1336),
	// Message: This will terminate the replay.  Do you wish to continue?
	THIS_WILL_TERMINATE_THE_REPLAY__DO_YOU_WISH_TO_CONTINUE(1337),
	// Message: You have exceeded the maximum amount that may be transferred at one time.
	YOU_HAVE_EXCEEDED_THE_MAXIMUM_AMOUNT_THAT_MAY_BE_TRANSFERRED_AT_ONE_TIME(1338),
	// Message: Once a macro is assigned to a shortcut, it cannot be run as a macro again.
	ONCE_A_MACRO_IS_ASSIGNED_TO_A_SHORTCUT_IT_CANNOT_BE_RUN_AS_A_MACRO_AGAIN(1339),
	// Message: This server cannot be accessed by the coupon you are using.
	THIS_SERVER_CANNOT_BE_ACCESSED_BY_THE_COUPON_YOU_ARE_USING(1340),
	// Message: Incorrect name and/or email address.
	INCORRECT_NAME_ANDOR_EMAIL_ADDRESS(1341),
	// Message: You are already logged in.
	YOU_ARE_ALREADY_LOGGED_IN(1342),
	// Message: Incorrect email address and/or password.  Your attempt to log into .NET Messenger Service has failed.
	INCORRECT_EMAIL_ADDRESS_ANDOR_PASSWORD__YOUR_ATTEMPT_TO_LOG_INTO_NET_MESSENGER_SERVICE_HAS_FAILED(1343),
	// Message: Your request to log into the .NET Messenger Service has failed.  Please verify that you are currently connected to the internet.
	YOUR_REQUEST_TO_LOG_INTO_THE_NET_MESSENGER_SERVICE_HAS_FAILED__PLEASE_VERIFY_THAT_YOU_ARE_CURRENTLY_CONNECTED_TO_THE_INTERNET(1344),
	// Message: Click on the OK button after you have selected a contact name.
	CLICK_ON_THE_OK_BUTTON_AFTER_YOU_HAVE_SELECTED_A_CONTACT_NAME(1345),
	// Message: You are currently entering a chat message.
	YOU_ARE_CURRENTLY_ENTERING_A_CHAT_MESSAGE(1346),
	// Message: The Lineage II messenger could not carry out the task you requested.
	THE_LINEAGE_II_MESSENGER_COULD_NOT_CARRY_OUT_THE_TASK_YOU_REQUESTED(1347),
	// Message: $s1 has entered the chat room.
	S1_HAS_ENTERED_THE_CHAT_ROOM(1348),
	// Message: $s1 has left the chat room.
	S1_HAS_LEFT_THE_CHAT_ROOM(1349),
	// Message: The status will be changed to indicate "off-line." All the chat windows currently opened will be closed.
	THE_STATUS_WILL_BE_CHANGED_TO_INDICATE_OFFLINE_ALL_THE_CHAT_WINDOWS_CURRENTLY_OPENED_WILL_BE_CLOSED(1350),
	// Message: Click the Delete button after selecting the contact you wish to remove.
	CLICK_THE_DELETE_BUTTON_AFTER_SELECTING_THE_CONTACT_YOU_WISH_TO_REMOVE(1351),
	// Message: You have been added to $s1 ($s2)'s contact list.
	YOU_HAVE_BEEN_ADDED_TO_S1_S2S_CONTACT_LIST(1352),
	// Message: You can set the option to show your status as always being off-line to all of your contacts.
	YOU_CAN_SET_THE_OPTION_TO_SHOW_YOUR_STATUS_AS_ALWAYS_BEING_OFFLINE_TO_ALL_OF_YOUR_CONTACTS(1353),
	// Message: You are not allowed to chat with a contact while a chatting block is imposed.
	YOU_ARE_NOT_ALLOWED_TO_CHAT_WITH_A_CONTACT_WHILE_A_CHATTING_BLOCK_IS_IMPOSED(1354),
	// Message: That contact is currently blocked from chatting.
	THAT_CONTACT_IS_CURRENTLY_BLOCKED_FROM_CHATTING(1355),
	// Message: That contact is not currently logged in.
	THAT_CONTACT_IS_NOT_CURRENTLY_LOGGED_IN(1356),
	// Message: You have been blocked from chatting with that contact.
	YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT(1357),
	// Message: You are being logged out...
	YOU_ARE_BEING_LOGGED_OUT(1358),
	// Message: $s1 has logged in.
	S1_HAS_LOGGED_IN_(1359),
	// Message: You have received a message from $s1.
	YOU_HAVE_RECEIVED_A_MESSAGE_FROM_S1(1360),
	// Message: Due to a system error, you have been logged out of the .NET Messenger Service.
	DUE_TO_A_SYSTEM_ERROR_YOU_HAVE_BEEN_LOGGED_OUT_OF_THE_NET_MESSENGER_SERVICE(1361),
	// Message: Please select the contact you wish to delete.  If you would like to delete a group, click the button next to My Status, and then use the Options menu.
	PLEASE_SELECT_THE_CONTACT_YOU_WISH_TO_DELETE__IF_YOU_WOULD_LIKE_TO_DELETE_A_GROUP_CLICK_THE_BUTTON_NEXT_TO_MY_STATUS_AND_THEN_USE_THE_OPTIONS_MENU(1362),
	// Message: Your request to participate in the alliance war has been denied.
	YOUR_REQUEST_TO_PARTICIPATE_IN_THE_ALLIANCE_WAR_HAS_BEEN_DENIED(1363),
	// Message: The request for an alliance war has been rejected.
	THE_REQUEST_FOR_AN_ALLIANCE_WAR_HAS_BEEN_REJECTED(1364),
	// Message: $s2 of $s1 clan has surrendered as an individual.
	S2_OF_S1_CLAN_HAS_SURRENDERED_AS_AN_INDIVIDUAL(1365),
	// Message: In order to delete a group, you must not have any contacts listed under that group.  Please transfer your contact(s) to another group before continuing with deletion.
	IN_ORDER_TO_DELETE_A_GROUP_YOU_MUST_NOT_HAVE_ANY_CONTACTS_LISTED_UNDER_THAT_GROUP__PLEASE_TRANSFER_YOUR_CONTACTS_TO_ANOTHER_GROUP_BEFORE_CONTINUING_WITH_DELETION(1366),
	// Message: Only members of the group are allowed to add records.
	ONLY_MEMBERS_OF_THE_GROUP_ARE_ALLOWED_TO_ADD_RECORDS(1367),
	// Message: You can not try those items on at the same time.
	YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME(1368),
	// Message: You've exceeded the maximum.
	YOUVE_EXCEEDED_THE_MAXIMUM(1369),
	// Message: Your message to $s1 did not reach it's recipient.  You cannot send mail to the GM staff.
	YOUR_MESSAGE_TO_S1_DID_NOT_REACH_ITS_RECIPIENT__YOU_CANNOT_SEND_MAIL_TO_THE_GM_STAFF(1370),
	// Message: It has been determined that you're not engaged in normal gameplay and a restriction has been imposed upon you. You may not move for $s1 minutes.
	IT_HAS_BEEN_DETERMINED_THAT_YOURE_NOT_ENGAGED_IN_NORMAL_GAMEPLAY_AND_A_RESTRICTION_HAS_BEEN_IMPOSED_UPON_YOU_YOU_MAY_NOT_MOVE_FOR_S1_MINUTES(1371),
	// Message: Your punishment will continue for $s1 minutes.
	YOUR_PUNISHMENT_WILL_CONTINUE_FOR_S1_MINUTES(1372),
	// Message: $s1 has picked up $s2 that was dropped by a Raid Boss.
	S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_A_RAID_BOSS(1373),
	// Message: $s1 has picked up $s3 $s2(s) that was dropped by a Raid Boss.
	S1_HAS_PICKED_UP_S3_S2S_THAT_WAS_DROPPED_BY_A_RAID_BOSS(1374),
	// Message: $s1 has picked up  $s2 adena that was dropped by a Raid Boss.
	S1_HAS_PICKED_UP__S2_ADENA_THAT_WAS_DROPPED_BY_A_RAID_BOSS(1375),
	// Message: $s1 has picked up $s2 that was dropped by another character.
	S1_HAS_PICKED_UP_S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER(1376),
	// Message: $s1 has picked up $s3 $s2(s) that was dropped by another character.
	S1_HAS_PICKED_UP_S3_S2S_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER(1377),
	// Message: $s1 has picked up +$s3$s2 that was dropped by another character.
	S1_HAS_PICKED_UP_S3S2_THAT_WAS_DROPPED_BY_ANOTHER_CHARACTER(1378),
	// Message: $s1 has obtained $s2 adena.
	S1_HAS_OBTAINED_S2_ADENA(1379),
	// Message: You can't summon a $s1 while on the battleground.
	YOU_CANT_SUMMON_A_S1_WHILE_ON_THE_BATTLEGROUND(1380),
	// Message: The party leader has obtained $s2 of $s1.
	THE_PARTY_LEADER_HAS_OBTAINED_S2_OF_S1(1381),
	// Message: To fulfill the quest, you must bring the chosen weapon.  Are you sure you want to choose this weapon?
	TO_FULFILL_THE_QUEST_YOU_MUST_BRING_THE_CHOSEN_WEAPON__ARE_YOU_SURE_YOU_WANT_TO_CHOOSE_THIS_WEAPON(1382),
	// Message: Are you sure you want to exchange?
	ARE_YOU_SURE_YOU_WANT_TO_EXCHANGE(1383),
	// Message: $s1 has become the party leader.
	S1_HAS_BECOME_THE_PARTY_LEADER(1384),
	// Message: You are not allowed to dismount at this location.
	YOU_ARE_NOT_ALLOWED_TO_DISMOUNT_AT_THIS_LOCATION(1385),
	// Message: You are no longer held in place.
	YOU_ARE_NO_LONGER_HELD_IN_PLACE(1386),
	// Message: Please select the item you would like to try on.
	PLEASE_SELECT_THE_ITEM_YOU_WOULD_LIKE_TO_TRY_ON(1387),
	// Message: A party room has been created.
	A_PARTY_ROOM_HAS_BEEN_CREATED(1388),
	// Message: The party room's information has been revised.
	THE_PARTY_ROOMS_INFORMATION_HAS_BEEN_REVISED(1389),
	// Message: You are not allowed to enter the party room.
	YOU_ARE_NOT_ALLOWED_TO_ENTER_THE_PARTY_ROOM(1390),
	// Message: You have exited from the party room.
	YOU_HAVE_EXITED_FROM_THE_PARTY_ROOM(1391),
	// Message: $s1 has left the party room.
	S1_HAS_LEFT_THE_PARTY_ROOM(1392),
	// Message: You have been ousted from the party room.
	YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM(1393),
	// Message: $s1 has been ousted from the party room.
	S1_HAS_BEEN_OUSTED_FROM_THE_PARTY_ROOM(1394),
	// Message: The party room has been disbanded.
	THE_PARTY_ROOM_HAS_BEEN_DISBANDED(1395),
	// Message: The list of party rooms can only be viewed by a person who has not joined a party or who is currently the leader of a party.
	THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_HAS_NOT_JOINED_A_PARTY_OR_WHO_IS_CURRENTLY_THE_LEADER_OF_A_PARTY(1396),
	// Message: The leader of the party room has changed.
	THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED(1397),
	// Message: We are recruiting party members.
	WE_ARE_RECRUITING_PARTY_MEMBERS(1398),
	// Message: Only the leader of the party can transfer party leadership to another player.
	ONLY_THE_LEADER_OF_THE_PARTY_CAN_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_PLAYER(1399),
	// Message: Please select the person you wish to make the party leader.
	PLEASE_SELECT_THE_PERSON_YOU_WISH_TO_MAKE_THE_PARTY_LEADER(1400),
	// Message: Slow down…you are already the party leader.
	SLOW_DOWNYOU_ARE_ALREADY_THE_PARTY_LEADER(1401),
	// Message: You may only transfer party leadership to another member of the party.
	YOU_MAY_ONLY_TRANSFER_PARTY_LEADERSHIP_TO_ANOTHER_MEMBER_OF_THE_PARTY(1402),
	// Message: You have failed to transfer the party leadership.
	YOU_HAVE_FAILED_TO_TRANSFER_THE_PARTY_LEADERSHIP(1403),
	// Message: The owner of the private manufacturing store has changed the price for creating this item.  Please check the new price before trying again.
	THE_OWNER_OF_THE_PRIVATE_MANUFACTURING_STORE_HAS_CHANGED_THE_PRICE_FOR_CREATING_THIS_ITEM__PLEASE_CHECK_THE_NEW_PRICE_BEFORE_TRYING_AGAIN(1404),
	// Message: $s1's CPs have been restored.
	S1S_CPS_HAVE_BEEN_RESTORED(1405),
	// Message: $s1 restores $s2 CP.
	S1_RESTORES_S2_CP(1406),
	// Message: You are using a computer that does not allow you to log in with two accounts at the same time.
	YOU_ARE_USING_A_COMPUTER_THAT_DOES_NOT_ALLOW_YOU_TO_LOG_IN_WITH_TWO_ACCOUNTS_AT_THE_SAME_TIME(1407),
	// Message: Your prepaid remaining usage time is $s1 hours and $s2 minutes.  You have $s3 paid reservations left.
	YOUR_PREPAID_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES__YOU_HAVE_S3_PAID_RESERVATIONS_LEFT(1408),
	// Message: Your prepaid usage time has expired. Your new prepaid reservation will be used. The remaining usage time is $s1 hours and $s2 minutes.
	YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOUR_NEW_PREPAID_RESERVATION_WILL_BE_USED_THE_REMAINING_USAGE_TIME_IS_S1_HOURS_AND_S2_MINUTES(1409),
	// Message: Your prepaid usage time has expired. You do not have any more prepaid reservations left.
	YOUR_PREPAID_USAGE_TIME_HAS_EXPIRED_YOU_DO_NOT_HAVE_ANY_MORE_PREPAID_RESERVATIONS_LEFT(1410),
	// Message: The number of your prepaid reservations has changed.
	THE_NUMBER_OF_YOUR_PREPAID_RESERVATIONS_HAS_CHANGED(1411),
	// Message: Your prepaid usage time has $s1 minutes left.
	YOUR_PREPAID_USAGE_TIME_HAS_S1_MINUTES_LEFT(1412),
	// Message: You do not meet the requirements to enter that party room.
	YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM(1413),
	// Message: The width and length should be 100 or more grids and less than 5000 grids respectively.
	THE_WIDTH_AND_LENGTH_SHOULD_BE_100_OR_MORE_GRIDS_AND_LESS_THAN_5000_GRIDS_RESPECTIVELY(1414),
	// Message: The command file is not set.
	THE_COMMAND_FILE_IS_NOT_SET(1415),
	// Message: The party representative of Team 1 has not been selected.
	THE_PARTY_REPRESENTATIVE_OF_TEAM_1_HAS_NOT_BEEN_SELECTED(1416),
	// Message: The party representative of Team 2 has not been selected.
	THE_PARTY_REPRESENTATIVE_OF_TEAM_2_HAS_NOT_BEEN_SELECTED(1417),
	// Message: The name of Team 1 has not yet been chosen.
	THE_NAME_OF_TEAM_1_HAS_NOT_YET_BEEN_CHOSEN(1418),
	// Message: The name of Team 2 has not yet been chosen.
	THE_NAME_OF_TEAM_2_HAS_NOT_YET_BEEN_CHOSEN(1419),
	// Message: The name of Team 1 and the name of Team 2 are identical.
	THE_NAME_OF_TEAM_1_AND_THE_NAME_OF_TEAM_2_ARE_IDENTICAL(1420),
	// Message: The race setup file has not been designated.
	THE_RACE_SETUP_FILE_HAS_NOT_BEEN_DESIGNATED(1421),
	// Message: Race setup file error - BuffCnt is not specified.
	RACE_SETUP_FILE_ERROR__BUFFCNT_IS_NOT_SPECIFIED(1422),
	// Message: Race setup file error - BuffID$s1 is not specified.
	RACE_SETUP_FILE_ERROR__BUFFIDS1_IS_NOT_SPECIFIED(1423),
	// Message: Race setup file error - BuffLv$s1 is not specified.
	RACE_SETUP_FILE_ERROR__BUFFLVS1_IS_NOT_SPECIFIED(1424),
	// Message: Race setup file error - DefaultAllow is not specified.
	RACE_SETUP_FILE_ERROR__DEFAULTALLOW_IS_NOT_SPECIFIED(1425),
	// Message: Race setup file error - ExpSkillCnt is not specified.
	RACE_SETUP_FILE_ERROR__EXPSKILLCNT_IS_NOT_SPECIFIED(1426),
	// Message: Race setup file error - ExpSkillID$s1 is not specified.
	RACE_SETUP_FILE_ERROR__EXPSKILLIDS1_IS_NOT_SPECIFIED(1427),
	// Message: Race setup file error - ExpItemCnt is not specified.
	RACE_SETUP_FILE_ERROR__EXPITEMCNT_IS_NOT_SPECIFIED(1428),
	// Message: Race setup file error - ExpItemID$s1 is not specified.
	RACE_SETUP_FILE_ERROR__EXPITEMIDS1_IS_NOT_SPECIFIED(1429),
	// Message: Race setup file error -  TeleportDelay is not specified.
	RACE_SETUP_FILE_ERROR___TELEPORTDELAY_IS_NOT_SPECIFIED(1430),
	// Message: The race will be stopped temporarily.
	THE_RACE_WILL_BE_STOPPED_TEMPORARILY(1431),
	// Message: Your opponent is currently in a petrified state.
	YOUR_OPPONENT_IS_CURRENTLY_IN_A_PETRIFIED_STATE(1432),
	// Message: You will now automatically apply $s1 to your weapon.
	YOU_WILL_NOW_AUTOMATICALLY_APPLY_S1_TO_YOUR_WEAPON(1433),
	// Message: You will no longer automatically apply $s1 to your weapon.
	YOU_WILL_NO_LONGER_AUTOMATICALLY_APPLY_S1_TO_YOUR_WEAPON(1434),
	// Message: Due to insufficient $s1, the automatic use function has been cancelled.
	DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_HAS_BEEN_CANCELLED(1435),
	// Message: Due to insufficient $s1, the automatic use function cannot be activated.
	DUE_TO_INSUFFICIENT_S1_THE_AUTOMATIC_USE_FUNCTION_CANNOT_BE_ACTIVATED(1436),
	// Message: Players are no longer allowed to play dice. Dice can no longer be purchased from a village store. However, you can still sell them to any village store.
	PLAYERS_ARE_NO_LONGER_ALLOWED_TO_PLAY_DICE_DICE_CAN_NO_LONGER_BE_PURCHASED_FROM_A_VILLAGE_STORE_HOWEVER_YOU_CAN_STILL_SELL_THEM_TO_ANY_VILLAGE_STORE(1437),
	// Message: There is no skill that enables enchant.
	THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT(1438),
	// Message: You do not have all of the items needed to enchant that skill.
	YOU_DO_NOT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL(1439),
	// Message: You have succeeded in enchanting the skill $s1.
	YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1(1440),
	// Message: You have failed to enchant the skill $s1.
	YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1(1441),
	// Message: Remaining Time: $s1 second(s)
	REMAINING_TIME_S1_SECONDS(1442),
	// Message: You do not have enough SP to enchant that skill.
	YOU_DO_NOT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL(1443),
	// Message: You do not have enough experience (Exp) to enchant that skill.
	YOU_DO_NOT_HAVE_ENOUGH_EXPERIENCE_EXP_TO_ENCHANT_THAT_SKILL(1444),
	// Message: Your previous subclass will be removed and replaced with the new subclass at level 40.  Do you wish to continue?
	YOUR_PREVIOUS_SUBCLASS_WILL_BE_REMOVED_AND_REPLACED_WITH_THE_NEW_SUBCLASS_AT_LEVEL_40__DO_YOU_WISH_TO_CONTINUE(1445),
	// Message: The ferry from $s1 to $s2 has been delayed.
	THE_FERRY_FROM_S1_TO_S2_HAS_BEEN_DELAYED(1446),
	// Message: You cannot do that while fishing.
	YOU_CANNOT_DO_THAT_WHILE_FISHING(1447),
	// Message: Only fishing skills may be used at this time.
	ONLY_FISHING_SKILLS_MAY_BE_USED_AT_THIS_TIME(1448),
	// Message: You've got a bite!
	YOUVE_GOT_A_BITE(1449),
	// Message: That fish is more determined than you are - it spit the hook!
	THAT_FISH_IS_MORE_DETERMINED_THAN_YOU_ARE__IT_SPIT_THE_HOOK(1450),
	// Message: Your bait was stolen by that fish!
	YOUR_BAIT_WAS_STOLEN_BY_THAT_FISH(1451),
	// Message: Baits have been lost because the fish got away.
	BAITS_HAVE_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY(1452),
	// Message: You do not have a fishing pole equipped.
	YOU_DO_NOT_HAVE_A_FISHING_POLE_EQUIPPED(1453),
	// Message: You must put bait on your hook before you can fish.
	YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH(1454),
	// Message: You cannot fish while under water.
	YOU_CANNOT_FISH_WHILE_UNDER_WATER(1455),
	// Message: You cannot fish while riding as a passenger of a boat - it's against the rules.
	YOU_CANNOT_FISH_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT__ITS_AGAINST_THE_RULES(1456),
	// Message: You can't fish here.
	YOU_CANT_FISH_HERE(1457),
	// Message: Your attempt at fishing has been cancelled.
	YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED(1458),
	// Message: You do not have enough bait.
	YOU_DO_NOT_HAVE_ENOUGH_BAIT(1459),
	// Message: You reel your line in and stop fishing.
	YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING(1460),
	// Message: You cast your line and start to fish.
	YOU_CAST_YOUR_LINE_AND_START_TO_FISH(1461),
	// Message: You may only use the Pumping skill while you are fishing.
	YOU_MAY_ONLY_USE_THE_PUMPING_SKILL_WHILE_YOU_ARE_FISHING(1462),
	// Message: You may only use the Reeling skill while you are fishing.
	YOU_MAY_ONLY_USE_THE_REELING_SKILL_WHILE_YOU_ARE_FISHING(1463),
	// Message: The fish has resisted your attempt to bring it in.
	THE_FISH_HAS_RESISTED_YOUR_ATTEMPT_TO_BRING_IT_IN(1464),
	// Message: Your pumping is successful, causing $s1 damage.
	YOUR_PUMPING_IS_SUCCESSFUL_CAUSING_S1_DAMAGE(1465),
	// Message: You failed to do anything with the fish and it regains $s1 HP.
	YOU_FAILED_TO_DO_ANYTHING_WITH_THE_FISH_AND_IT_REGAINS_S1_HP(1466),
	// Message: You reel that fish in closer and cause $s1 damage.
	YOU_REEL_THAT_FISH_IN_CLOSER_AND_CAUSE_S1_DAMAGE(1467),
	// Message: You failed to reel that fish in further and it regains $s1 HP.
	YOU_FAILED_TO_REEL_THAT_FISH_IN_FURTHER_AND_IT_REGAINS_S1_HP(1468),
	// Message: You caught something!
	YOU_CAUGHT_SOMETHING(1469),
	// Message: You cannot do that while fishing.
	YOU_CANNOT_DO_THAT_WHILE_FISHING_(1470),
	// Message: You cannot do that while fishing.
	YOU_CANNOT_DO_THAT_WHILE_FISHING__(1471),
	// Message: You look oddly at the fishing pole in disbelief and realize that you can't attack anything with this.
	YOU_LOOK_ODDLY_AT_THE_FISHING_POLE_IN_DISBELIEF_AND_REALIZE_THAT_YOU_CANT_ATTACK_ANYTHING_WITH_THIS(1472),
	// Message: $s1 is not sufficient.
	S1_IS_NOT_SUFFICIENT(1473),
	// Message: $s1 is not available.
	S1_IS_NOT_AVAILABLE(1474),
	// Message: Pet has dropped $s1.
	PET_HAS_DROPPED_S1(1475),
	// Message: Pet has dropped +$s1$s2.
	PET_HAS_DROPPED_S1S2(1476),
	// Message: Pet has dropped $s2 of $s1.
	PET_HAS_DROPPED_S2_OF_S1(1477),
	// Message: You may only register a 64 x 64 pixel, 256-color BMP.
	YOU_MAY_ONLY_REGISTER_A_64_X_64_PIXEL_256COLOR_BMP(1478),
	// Message: That is the wrong grade of soulshot for that fishing pole.
	THAT_IS_THE_WRONG_GRADE_OF_SOULSHOT_FOR_THAT_FISHING_POLE(1479),
	// Message: Are you sure you wish to remove yourself from the Grand Olympiad Games waiting list?
	ARE_YOU_SURE_YOU_WISH_TO_REMOVE_YOURSELF_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST(1480),
	// Message: You've selected to join a non-class specific game.  Continue?
	YOUVE_SELECTED_TO_JOIN_A_NONCLASS_SPECIFIC_GAME__CONTINUE(1481),
	// Message: You've selected to join a class specific game.  Continue?
	YOUVE_SELECTED_TO_JOIN_A_CLASS_SPECIFIC_GAME__CONTINUE(1482),
	// Message: Are you ready to be a Hero?
	ARE_YOU_READY_TO_BE_A_HERO(1483),
	// Message: Are you sure this is the Hero weapon you wish to use?
	ARE_YOU_SURE_THIS_IS_THE_HERO_WEAPON_YOU_WISH_TO_USE(1484),
	// Message: The ferry from Talking Island to Gludin Harbor has been delayed.
	THE_FERRY_FROM_TALKING_ISLAND_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED(1485),
	// Message: The ferry from Gludin Harbor to Talking Island has been delayed.
	THE_FERRY_FROM_GLUDIN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED(1486),
	// Message: The ferry from Giran Harbor to Talking Island has been delayed.
	THE_FERRY_FROM_GIRAN_HARBOR_TO_TALKING_ISLAND_HAS_BEEN_DELAYED(1487),
	// Message: The ferry from Talking Island to Giran Harbor has been delayed.
	THE_FERRY_FROM_TALKING_ISLAND_TO_GIRAN_HARBOR_HAS_BEEN_DELAYED(1488),
	// Message: Innadril cruise service has been delayed.
	INNADRIL_CRUISE_SERVICE_HAS_BEEN_DELAYED(1489),
	// Message: Traded $s2 of crop $s1.
	TRADED_S2_OF_CROP_S1(1490),
	// Message: Failed in trading $s2 of crop $s1.
	FAILED_IN_TRADING_S2_OF_CROP_S1(1491),
	// Message: You will be moved to the Olympiad Stadium in $s1 second(s).
	YOU_WILL_BE_MOVED_TO_THE_OLYMPIAD_STADIUM_IN_S1_SECONDS(1492),
	// Message: Your opponent made haste with their tail between their legs; the match has been cancelled.
	YOUR_OPPONENT_MADE_HASTE_WITH_THEIR_TAIL_BETWEEN_THEIR_LEGS_THE_MATCH_HAS_BEEN_CANCELLED(1493),
	// Message: Your opponent does not meet the requirements to do battle; the match has been cancelled.
	YOUR_OPPONENT_DOES_NOT_MEET_THE_REQUIREMENTS_TO_DO_BATTLE_THE_MATCH_HAS_BEEN_CANCELLED(1494),
	// Message: The Grand Olympiad match will start in $s1 second(s).
	THE_GRAND_OLYMPIAD_MATCH_WILL_START_IN_S1_SECONDS(1495),
	// Message: The match has started, fight!
	THE_MATCH_HAS_STARTED_FIGHT(1496),
	// Message: Congratulations $s1, you win the match!
	CONGRATULATIONS_S1_YOU_WIN_THE_MATCH(1497),
	// Message: There is no victor; the match ends in a tie.
	THERE_IS_NO_VICTOR_THE_MATCH_ENDS_IN_A_TIE(1498),
	// Message: You will be moved back to town in $s1 second(s).
	YOU_WILL_BE_MOVED_BACK_TO_TOWN_IN_S1_SECONDS(1499),
	// Message: You cannot participate in the Grand Olympiad Games with a character in their subclass.
	YOU_CANNOT_PARTICIPATE_IN_THE_GRAND_OLYMPIAD_GAMES_WITH_A_CHARACTER_IN_THEIR_SUBCLASS(1500),
	// Message: Only Noblesse can participate in the Olympiad.
	ONLY_NOBLESSE_CAN_PARTICIPATE_IN_THE_OLYMPIAD(1501),
	// Message: You have already been registered in a waiting list of an event.
	YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT(1502),
	// Message: You have been registered in the Grand Olympiad Games waiting list for a class specific match.
	YOU_HAVE_BEEN_REGISTERED_IN_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH(1503),
	// Message: You have been registered in the Grand Olympiad Games waiting list for a non-class specific match.
	YOU_HAVE_BEEN_REGISTERED_IN_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST_FOR_A_NONCLASS_SPECIFIC_MATCH(1504),
	// Message: You have been removed from the Grand Olympiad Games waiting list.
	YOU_HAVE_BEEN_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST(1505),
	// Message: You are not currently registered on any Grand Olympiad Games waiting list.
	YOU_ARE_NOT_CURRENTLY_REGISTERED_ON_ANY_GRAND_OLYMPIAD_GAMES_WAITING_LIST(1506),
	// Message: You cannot equip that item in a Grand Olympiad Games match.
	YOU_CANNOT_EQUIP_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH(1507),
	// Message: You cannot use that item in a Grand Olympiad Games match.
	YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH(1508),
	// Message: You cannot use that skill in a Grand Olympiad Games match.
	YOU_CANNOT_USE_THAT_SKILL_IN_A_GRAND_OLYMPIAD_GAMES_MATCH(1509),
	// Message: $s1 is making an attempt at resurrection. Do you want to continue with this resurrection?
	S1_IS_MAKING_AN_ATTEMPT_AT_RESURRECTION_DO_YOU_WANT_TO_CONTINUE_WITH_THIS_RESURRECTION(1510),
	// Message: While a pet is attempting to resurrect, it cannot help in resurrecting its master.
	WHILE_A_PET_IS_ATTEMPTING_TO_RESURRECT_IT_CANNOT_HELP_IN_RESURRECTING_ITS_MASTER(1511),
	// Message: You cannot resurrect a pet while their owner is being resurrected.
	YOU_CANNOT_RESURRECT_A_PET_WHILE_THEIR_OWNER_IS_BEING_RESURRECTED(1512),
	// Message: Resurrection has already been proposed.
	RESURRECTION_HAS_ALREADY_BEEN_PROPOSED(1513),
	// Message: You cannot resurrect the owner of a pet while their pet is being resurrected.
	YOU_CANNOT_RESURRECT_THE_OWNER_OF_A_PET_WHILE_THEIR_PET_IS_BEING_RESURRECTED(1514),
	// Message: A pet cannot be resurrected while it's owner is in the process of resurrecting.
	A_PET_CANNOT_BE_RESURRECTED_WHILE_ITS_OWNER_IS_IN_THE_PROCESS_OF_RESURRECTING(1515),
	// Message: The target is unavailable for seeding.
	THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING(1516),
	// Message: Failed in Blessed Enchant. The enchant value of the item became 0.
	FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0(1517),
	// Message: You do not meet the required condition to equip that item.
	YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM(1518),
	// Message: Your pet has been killed!  Make sure you resurrect your pet within 20 minutes or your pet and all of it's items will disappear forever!
	YOUR_PET_HAS_BEEN_KILLED__MAKE_SURE_YOU_RESURRECT_YOUR_PET_WITHIN_20_MINUTES_OR_YOUR_PET_AND_ALL_OF_ITS_ITEMS_WILL_DISAPPEAR_FOREVER(1519),
	// Message: Servitor passed away.
	SERVITOR_PASSED_AWAY(1520),
	// Message: Your servitor has vanished!  You'll need to summon a new one.
	YOUR_SERVITOR_HAS_VANISHED__YOULL_NEED_TO_SUMMON_A_NEW_ONE(1521),
	// Message: Your pet's corpse has decayed!
	YOUR_PETS_CORPSE_HAS_DECAYED(1522),
	// Message: You should release your pet or servitor so that it does not fall off of the boat and drown!
	YOU_SHOULD_RELEASE_YOUR_PET_OR_SERVITOR_SO_THAT_IT_DOES_NOT_FALL_OFF_OF_THE_BOAT_AND_DROWN(1523),
	// Message: $s1's pet gained $s2.
	S1S_PET_GAINED_S2(1524),
	// Message: $s1's pet gained $s3 of $s2.
	S1S_PET_GAINED_S3_OF_S2(1525),
	// Message: $s1's pet gained +$s2$s3.
	S1S_PET_GAINED_S2S3(1526),
	// Message: Your pet was hungry so it ate $s1.
	YOUR_PET_WAS_HUNGRY_SO_IT_ATE_S1(1527),
	// Message: You've sent a petition to the GM staff.
	YOUVE_SENT_A_PETITION_TO_THE_GM_STAFF(1528),
	// Message: $s1 has invited you to join a guild. Do you wish to accept?
	S1_HAS_INVITED_YOU_TO_JOIN_A_GUILD_DO_YOU_WISH_TO_ACCEPT(1529),
	// Message: Select a target or enter the name.
	SELECT_A_TARGET_OR_ENTER_THE_NAME(1530),
	// Message: Enter the name of the clan that you wish to declare war on.
	ENTER_THE_NAME_OF_THE_CLAN_THAT_YOU_WISH_TO_DECLARE_WAR_ON(1531),
	// Message: Enter the name of the clan that you wish to have a cease-fire with.
	ENTER_THE_NAME_OF_THE_CLAN_THAT_YOU_WISH_TO_HAVE_A_CEASEFIRE_WITH(1532),
	// Message: Attention: $s1 picked up $s2.
	ATTENTION_S1_PICKED_UP_S2(1533),
	// Message: Attention: $s1 picked up +$s2 $s3.
	ATTENTION_S1_PICKED_UP_S2_S3(1534),
	// Message: Attention: $s1's pet picked up $s2.
	ATTENTION_S1S_PET_PICKED_UP_S2(1535),
	// Message: Attention: $s1's pet picked up +$s2 $s3.
	ATTENTION_S1S_PET_PICKED_UP_S2_S3(1536),
	// Message: Current Location:  $s1, $s2, $s3 (near Rune Village)
	CURRENT_LOCATION__S1_S2_S3_NEAR_RUNE_VILLAGE(1537),
	// Message: Current Location: $s1, $s2, $s3 (near the Town of Goddard)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_GODDARD(1538),
	// Message: Cargo has arrived at Talking Island Village.
	CARGO_HAS_ARRIVED_AT_TALKING_ISLAND_VILLAGE(1539),
	// Message: Cargo has arrived at the Dark Elf Village.
	CARGO_HAS_ARRIVED_AT_THE_DARK_ELF_VILLAGE(1540),
	// Message: Cargo has arrived at Elven Village.
	CARGO_HAS_ARRIVED_AT_ELVEN_VILLAGE(1541),
	// Message: Cargo has arrived at Orc Village.
	CARGO_HAS_ARRIVED_AT_ORC_VILLAGE(1542),
	// Message: Cargo has arrived at Dwarven Village.
	CARGO_HAS_ARRIVED_AT_DWARVEN_VILLAGE(1543),
	// Message: Cargo has arrived at Aden Castle Town.
	CARGO_HAS_ARRIVED_AT_ADEN_CASTLE_TOWN(1544),
	// Message: Cargo has arrived at the Town of Oren.
	CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_OREN(1545),
	// Message: Cargo has arrived at Hunters Village.
	CARGO_HAS_ARRIVED_AT_HUNTERS_VILLAGE(1546),
	// Message: Cargo has arrived at the Town of Dion.
	CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_DION(1547),
	// Message: Cargo has arrived at Floran Village.
	CARGO_HAS_ARRIVED_AT_FLORAN_VILLAGE(1548),
	// Message: Cargo has arrived at Gludin Village.
	CARGO_HAS_ARRIVED_AT_GLUDIN_VILLAGE(1549),
	// Message: Cargo has arrived at the Town of Gludio.
	CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_GLUDIO(1550),
	// Message: Cargo has arrived at Giran Castle Town.
	CARGO_HAS_ARRIVED_AT_GIRAN_CASTLE_TOWN(1551),
	// Message: Cargo has arrived at Heine.
	CARGO_HAS_ARRIVED_AT_HEINE(1552),
	// Message: Cargo has arrived at Rune Village.
	CARGO_HAS_ARRIVED_AT_RUNE_VILLAGE(1553),
	// Message: Cargo has arrived at the Town of Goddard.
	CARGO_HAS_ARRIVED_AT_THE_TOWN_OF_GODDARD(1554),
	// Message: Do you want to cancel character deletion?
	DO_YOU_WANT_TO_CANCEL_CHARACTER_DELETION(1555),
	// Message: Your clan notice has been saved.
	YOUR_CLAN_NOTICE_HAS_BEEN_SAVED(1556),
	// Message: Seed price should be more than $s1 and less than $s2.
	SEED_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2(1557),
	// Message: The quantity of seed should be more than $s1 and less than $s2.
	THE_QUANTITY_OF_SEED_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2(1558),
	// Message: Crop price should be more than $s1 and less than $s2.
	CROP_PRICE_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2(1559),
	// Message: The quantity of crop should be more than $s1 and less than $s2 .
	THE_QUANTITY_OF_CROP_SHOULD_BE_MORE_THAN_S1_AND_LESS_THAN_S2_(1560),
	// Message: The clan, $s1, has declared a Clan War.
	THE_CLAN_S1_HAS_DECLARED_A_CLAN_WAR(1561),
	// Message: A Clan War has been declared against the clan, $s1.  If you are killed during the Clan War by members of the opposing clan, you will only lose a quarter of the normal experience from death.
	A_CLAN_WAR_HAS_BEEN_DECLARED_AGAINST_THE_CLAN_S1__IF_YOU_ARE_KILLED_DURING_THE_CLAN_WAR_BY_MEMBERS_OF_THE_OPPOSING_CLAN_YOU_WILL_ONLY_LOSE_A_QUARTER_OF_THE_NORMAL_EXPERIENCE_FROM_DEATH(1562),
	// Message: The clan, $s1, cannot declare a Clan War because their clan is less than level three, and or they do not have enough members.
	THE_CLAN_S1_CANNOT_DECLARE_A_CLAN_WAR_BECAUSE_THEIR_CLAN_IS_LESS_THAN_LEVEL_THREE_AND_OR_THEY_DO_NOT_HAVE_ENOUGH_MEMBERS(1563),
	// Message: A Clan War can be declared only if the clan is level three or above, and the number of clan members is fifteen or greater.
	A_CLAN_WAR_CAN_BE_DECLARED_ONLY_IF_THE_CLAN_IS_LEVEL_THREE_OR_ABOVE_AND_THE_NUMBER_OF_CLAN_MEMBERS_IS_FIFTEEN_OR_GREATER(1564),
	// Message: A Clan War cannot be declared against a clan that does not exist!
	A_CLAN_WAR_CANNOT_BE_DECLARED_AGAINST_A_CLAN_THAT_DOES_NOT_EXIST(1565),
	// Message: The clan, $s1, has decided to stop the war.
	THE_CLAN_S1_HAS_DECIDED_TO_STOP_THE_WAR(1566),
	// Message: The war against $s1 Clan has been stopped.
	THE_WAR_AGAINST_S1_CLAN_HAS_BEEN_STOPPED(1567),
	// Message: The target for declaration is wrong.
	THE_TARGET_FOR_DECLARATION_IS_WRONG(1568),
	// Message: A declaration of Clan War against an allied clan can't be made.
	A_DECLARATION_OF_CLAN_WAR_AGAINST_AN_ALLIED_CLAN_CANT_BE_MADE(1569),
	// Message: A declaration of war against more than 30 Clans can't be made at the same time.
	A_DECLARATION_OF_WAR_AGAINST_MORE_THAN_30_CLANS_CANT_BE_MADE_AT_THE_SAME_TIME(1570),
	// Message: ======<Clans You've Declared War On>======
	CLANS_YOUVE_DECLARED_WAR_ON(1571),
	// Message: ======<Clans That Have Declared War On You>======
	CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU(1572),
	// Message: There are no clans that your clan has declared war against.
	THERE_ARE_NO_CLANS_THAT_YOUR_CLAN_HAS_DECLARED_WAR_AGAINST(1573),
	// Message: All is well.  There are no clans that have declared war against your clan.
	ALL_IS_WELL__THERE_ARE_NO_CLANS_THAT_HAVE_DECLARED_WAR_AGAINST_YOUR_CLAN(1574),
	// Message: Guilds can only be formed by a party leader who is also the leader of a level 5 clan.
	GUILDS_CAN_ONLY_BE_FORMED_BY_A_PARTY_LEADER_WHO_IS_ALSO_THE_LEADER_OF_A_LEVEL_5_CLAN(1575),
	// Message: Pet uses the power of spirit.
	PET_USES_THE_POWER_OF_SPIRIT(1576),
	// Message: Servitor uses the power of spirit.
	SERVITOR_USES_THE_POWER_OF_SPIRIT(1577),
	// Message: Items are not available for a private store or private manufacture.
	ITEMS_ARE_NOT_AVAILABLE_FOR_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURE(1578),
	// Message: $s1's pet gained $s2 adena.
	S1S_PET_GAINED_S2_ADENA(1579),
	// Message: The guild has been formed.
	THE_GUILD_HAS_BEEN_FORMED(1580),
	// Message: The guild has been disbanded.
	THE_GUILD_HAS_BEEN_DISBANDED(1581),
	// Message: You have joined the guild.
	YOU_HAVE_JOINED_THE_GUILD(1582),
	// Message: You were dismissed from the guild.
	YOU_WERE_DISMISSED_FROM_THE_GUILD(1583),
	// Message: $s1's party has been dismissed from the guild.
	S1S_PARTY_HAS_BEEN_DISMISSED_FROM_THE_GUILD(1584),
	// Message: The guild has been disbanded.
	THE_GUILD_HAS_BEEN_DISBANDED_(1585),
	// Message: You have quit the guild.
	YOU_HAVE_QUIT_THE_GUILD(1586),
	// Message: $s1's party has left the command channel.
	S1S_PARTY_HAS_LEFT_THE_COMMAND_CHANNEL(1587),
	// Message: The guild is activated only when there are at least 5 parties participating.
	THE_GUILD_IS_ACTIVATED_ONLY_WHEN_THERE_ARE_AT_LEAST_5_PARTIES_PARTICIPATING(1588),
	// Message: The guild's authority has been transferred to $s1.
	THE_GUILDS_AUTHORITY_HAS_BEEN_TRANSFERRED_TO_S1(1589),
	// Message: ===<Guild Info (Total Parties: $s1)>===
	GUILD_INFO_TOTAL_PARTIES_S1(1590),
	// Message: No user has been invited to the guild.
	NO_USER_HAS_BEEN_INVITED_TO_THE_GUILD(1591),
	// Message: You can no longer set up a guild.
	YOU_CAN_NO_LONGER_SET_UP_A_GUILD(1592),
	// Message: You do not have authority to invite someone to the guild.
	YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_GUILD(1593),
	// Message: $s1's party is already a member of the guild.
	S1S_PARTY_IS_ALREADY_A_MEMBER_OF_THE_GUILD(1594),
	// Message: $s1 has succeeded.
	S1_HAS_SUCCEEDED(1595),
	// Message: You were hit by $s1!
	YOU_WERE_HIT_BY_S1(1596),
	// Message: $s1 has failed.
	S1_HAS_FAILED(1597),
	// Message: Soulshots and spiritshots are not available for a dead pet or servitor.  Sad, isn't it?
	SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET_OR_SERVITOR__SAD_ISNT_IT(1598),
	// Message: You cannot "observe" while you are in combat!
	YOU_CANNOT_OBSERVE_WHILE_YOU_ARE_IN_COMBAT(1599),
	// Message: Tomorrow's items will ALL be set to 0.  Do you wish to continue?
	TOMORROWS_ITEMS_WILL_ALL_BE_SET_TO_0__DO_YOU_WISH_TO_CONTINUE(1600),
	// Message: Tomorrow's items will all be set to the same value as today's items.  Do you wish to continue?
	TOMORROWS_ITEMS_WILL_ALL_BE_SET_TO_THE_SAME_VALUE_AS_TODAYS_ITEMS__DO_YOU_WISH_TO_CONTINUE(1601),
	// Message: Only a party leader may access guild chat.
	ONLY_A_PARTY_LEADER_MAY_ACCESS_GUILD_CHAT(1602),
	// Message: Only channel opener can give All Command.
	ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND(1603),
	// Message: While dressed in formal wear, you can't use items that require all skills and casting operations.
	WHILE_DRESSED_IN_FORMAL_WEAR_YOU_CANT_USE_ITEMS_THAT_REQUIRE_ALL_SKILLS_AND_CASTING_OPERATIONS(1604),
	// Message: * Here, you can buy only seeds of $s1 Manor.
	_HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR(1605),
	// Message: Congratulations - You've completed the third-class transfer quest!
	CONGRATULATIONS__YOUVE_COMPLETED_THE_THIRDCLASS_TRANSFER_QUEST(1606),
	// Message: $s1 adena has been withdrawn to pay for purchasing fees.
	S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES(1607),
	// Message: Due to insufficient adena you cannot buy another castle.
	DUE_TO_INSUFFICIENT_ADENA_YOU_CANNOT_BUY_ANOTHER_CASTLE(1608),
	// Message: War has already been declared against that clan… but I'll make note that you really don't like them.
	WAR_HAS_ALREADY_BEEN_DECLARED_AGAINST_THAT_CLAN_BUT_ILL_MAKE_NOTE_THAT_YOU_REALLY_DONT_LIKE_THEM(1609),
	// Message: Fool! You cannot declare war against your own clan!
	FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN(1610),
	// Message: Party Leader: $s1
	PARTY_LEADER_S1(1611),
	// Message: =====<War List>=====
	WAR_LIST(1612),
	// Message: There is no clan listed on War List.
	THERE_IS_NO_CLAN_LISTED_ON_WAR_LIST(1613),
	// Message: You have joined a channel that was already open.
	YOU_HAVE_JOINED_A_CHANNEL_THAT_WAS_ALREADY_OPEN(1614),
	// Message: The number of remaining parties is $s1 until a channel is activated.
	THE_NUMBER_OF_REMAINING_PARTIES_IS_S1_UNTIL_A_CHANNEL_IS_ACTIVATED(1615),
	// Message: The guild has been activated.
	THE_GUILD_HAS_BEEN_ACTIVATED(1616),
	// Message: You do not have the authority to use the guild.
	YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_USE_THE_GUILD(1617),
	// Message: The ferry from Rune Harbor to Gludin Harbor has been delayed.
	THE_FERRY_FROM_RUNE_HARBOR_TO_GLUDIN_HARBOR_HAS_BEEN_DELAYED(1618),
	// Message: The ferry from Gludin Harbor to Rune Harbor has been delayed.
	THE_FERRY_FROM_GLUDIN_HARBOR_TO_RUNE_HARBOR_HAS_BEEN_DELAYED(1619),
	// Message: Arrived at Rune Harbor.
	ARRIVED_AT_RUNE_HARBOR(1620),
	// Message: Departure for Gludin Harbor will take place in five minutes!
	DEPARTURE_FOR_GLUDIN_HARBOR_WILL_TAKE_PLACE_IN_FIVE_MINUTES(1621),
	// Message: Departure for Gludin Harbor will take place in one minute!
	DEPARTURE_FOR_GLUDIN_HARBOR_WILL_TAKE_PLACE_IN_ONE_MINUTE(1622),
	// Message: Make haste!  We will be departing for Gludin Harbor shortly…
	MAKE_HASTE__WE_WILL_BE_DEPARTING_FOR_GLUDIN_HARBOR_SHORTLY(1623),
	// Message: We are now departing for Gludin Harbor.  Hold on and enjoy the ride!
	WE_ARE_NOW_DEPARTING_FOR_GLUDIN_HARBOR__HOLD_ON_AND_ENJOY_THE_RIDE(1624),
	// Message: Departure for Rune Harbor will take place after anchoring for ten minutes.
	DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_AFTER_ANCHORING_FOR_TEN_MINUTES(1625),
	// Message: Departure for Rune Harbor will take place in five minutes!
	DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_IN_FIVE_MINUTES(1626),
	// Message: Departure for Rune Harbor will take place in one minute!
	DEPARTURE_FOR_RUNE_HARBOR_WILL_TAKE_PLACE_IN_ONE_MINUTE(1627),
	// Message: Make haste!  We will be departing for Gludin Harbor shortly…
	MAKE_HASTE__WE_WILL_BE_DEPARTING_FOR_GLUDIN_HARBOR_SHORTLY_(1628),
	// Message: We are now departing for Rune Harbor.  Hold on and enjoy the ride!
	WE_ARE_NOW_DEPARTING_FOR_RUNE_HARBOR__HOLD_ON_AND_ENJOY_THE_RIDE(1629),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 15 minutes.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_15_MINUTES(1630),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 10 minutes.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_10_MINUTES(1631),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 5 minutes.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_5_MINUTES(1632),
	// Message: The ferry from Rune Harbor will be arriving at Gludin Harbor in approximately 1 minute.
	THE_FERRY_FROM_RUNE_HARBOR_WILL_BE_ARRIVING_AT_GLUDIN_HARBOR_IN_APPROXIMATELY_1_MINUTE(1633),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 15 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_15_MINUTES(1634),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 10 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_10_MINUTES(1635),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 5 minutes.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_5_MINUTES(1636),
	// Message: The ferry from Gludin Harbor will be arriving at Rune Harbor in approximately 1 minute.
	THE_FERRY_FROM_GLUDIN_HARBOR_WILL_BE_ARRIVING_AT_RUNE_HARBOR_IN_APPROXIMATELY_1_MINUTE(1637),
	// Message: You cannot fish while using a recipe book, private manufacture or private store.
	YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_MANUFACTURE_OR_PRIVATE_STORE(1638),
	// Message: Period $s1 of the Grand Olympiad Games has started!
	PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_STARTED(1639),
	// Message: Period $s1 of the Grand Olympiad Games has now ended.
	PERIOD_S1_OF_THE_GRAND_OLYMPIAD_GAMES_HAS_NOW_ENDED(1640),
	// Message: Sharpen your swords, tighten the stitchings in your armor, and make haste to a Grand Olympiad Manager!  Battles in the Grand Olympiad Games are now taking place!
	SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHINGS_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_GRAND_OLYMPIAD_MANAGER__BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE(1641),
	// Message: Much carnage has been left for the cleanup crew of the Olympiad Stadium.  Battles in the Grand Olympiad Games are now over!
	MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM__BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_OVER(1642),
	// Message: Current Location: $s1, $s2, $s3 (Dimensional Gap)
	CURRENT_LOCATION_S1_S2_S3_DIMENSIONAL_GAP(1643),
	// Message: none
	NONE(1644),
	// Message: none
	NONE_(1645),
	// Message: none
	NONE__(1646),
	// Message: none
	NONE___(1647),
	// Message: none
	NONE____(1648),
	// Message: Play time is now accumulating.
	PLAY_TIME_IS_NOW_ACCUMULATING(1649),
	// Message: Due to high server traffic, your login attempt has failed.  Please try again soon.
	DUE_TO_HIGH_SERVER_TRAFFIC_YOUR_LOGIN_ATTEMPT_HAS_FAILED__PLEASE_TRY_AGAIN_SOON(1650),
	// Message: The Grand Olympiad Games are not currently in progress.
	THE_GRAND_OLYMPIAD_GAMES_ARE_NOT_CURRENTLY_IN_PROGRESS(1651),
	// Message: You are now recording gameplay.
	YOU_ARE_NOW_RECORDING_GAMEPLAY(1652),
	// Message: Your recording has been successfully stored. ($s1)
	YOUR_RECORDING_HAS_BEEN_SUCCESSFULLY_STORED_S1(1653),
	// Message: The attempt to record the replay file has failed.
	THE_ATTEMPT_TO_RECORD_THE_REPLAY_FILE_HAS_FAILED(1654),
	// Message: You caught something smelly and scary, maybe you should throw it back!?
	YOU_CAUGHT_SOMETHING_SMELLY_AND_SCARY_MAYBE_YOU_SHOULD_THROW_IT_BACK(1655),
	// Message: You have successfully traded the item with the NPC.
	YOU_HAVE_SUCCESSFULLY_TRADED_THE_ITEM_WITH_THE_NPC(1656),
	// Message: $s1 has earned $s2 points in the Grand Olympiad Games.
	S1_HAS_EARNED_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES(1657),
	// Message: $s1 has lost $s2 points in the Grand Olympiad Games.
	S1_HAS_LOST_S2_POINTS_IN_THE_GRAND_OLYMPIAD_GAMES(1658),
	// Message: Current Location: $s1, $s2, $s3 (Cemetery of the Empire).
	CURRENT_LOCATION_S1_S2_S3_CEMETERY_OF_THE_EMPIRE(1659),
	// Message: The channel was opened by: $s1
	THE_CHANNEL_WAS_OPENED_BY_S1(1660),
	// Message: $s1 has obtained $s3 $s2s.
	S1_HAS_OBTAINED_S3_S2S(1661),
	// Message: The fish are no longer biting here because you've caught too many!  Try fishing in another location.
	THE_FISH_ARE_NO_LONGER_BITING_HERE_BECAUSE_YOUVE_CAUGHT_TOO_MANY__TRY_FISHING_IN_ANOTHER_LOCATION(1662),
	// Message: The clan crest was successfully registered.  Remember, only a clan that owns a clan hall or castle can have their crest displayed.
	THE_CLAN_CREST_WAS_SUCCESSFULLY_REGISTERED__REMEMBER_ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_CASTLE_CAN_HAVE_THEIR_CREST_DISPLAYED(1663),
	// Message: The fish is resisting your efforts to haul it in!  Look at that bobber go!
	THE_FISH_IS_RESISTING_YOUR_EFFORTS_TO_HAUL_IT_IN__LOOK_AT_THAT_BOBBER_GO(1664),
	// Message: You've worn that fish out!  It can't even pull the bobber under the water!
	YOUVE_WORN_THAT_FISH_OUT__IT_CANT_EVEN_PULL_THE_BOBBER_UNDER_THE_WATER(1665),
	// Message: You have obtained +$s1$s2.
	YOU_HAVE_OBTAINED_S1S2(1666),
	// Message: Lethal Strike!
	LETHAL_STRIKE(1667),
	// Message: Your lethal strike was successful!
	YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL(1668),
	// Message: There was nothing found inside of that.
	THERE_WAS_NOTHING_FOUND_INSIDE_OF_THAT(1669),
	// Message: Due to your Reeling and/or Pumping skill being three or more levels higher than your Fishing skill, a 50 damage penalty will be applied.
	DUE_TO_YOUR_REELING_ANDOR_PUMPING_SKILL_BEING_THREE_OR_MORE_LEVELS_HIGHER_THAN_YOUR_FISHING_SKILL_A_50_DAMAGE_PENALTY_WILL_BE_APPLIED(1670),
	// Message: Your reeling was successful! (Mastery Penalty:$s1 )
	YOUR_REELING_WAS_SUCCESSFUL_MASTERY_PENALTYS1_(1671),
	// Message: Your pumping was successful! (Mastery Penalty:$s1 )
	YOUR_PUMPING_WAS_SUCCESSFUL_MASTERY_PENALTYS1_(1672),
	// Message: Your current record for this Grand Olympiad is $s1 match(es), $s2 win(s) and $s3 defeat(s). You have earned $s4 Olympiad Point(s).
	YOUR_CURRENT_RECORD_FOR_THIS_GRAND_OLYMPIAD_IS_S1_MATCHES_S2_WINS_AND_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS(1673),
	// Message: This command can only be used by a Noblesse.
	THIS_COMMAND_CAN_ONLY_BE_USED_BY_A_NOBLESSE(1674),
	// Message: A manor cannot be set up between 6 a.m. and 8 p.m.
	A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM(1675),
	// Message: You do not have a servitor or pet and therefore cannot use the automatic-use function.
	YOU_DO_NOT_HAVE_A_SERVITOR_OR_PET_AND_THEREFORE_CANNOT_USE_THE_AUTOMATICUSE_FUNCTION(1676),
	// Message: A cease-fire during a Clan War can not be called while members of your clan are engaged in battle.
	A_CEASEFIRE_DURING_A_CLAN_WAR_CAN_NOT_BE_CALLED_WHILE_MEMBERS_OF_YOUR_CLAN_ARE_ENGAGED_IN_BATTLE(1677),
	// Message: You have not declared a Clan War against the clan $s1.
	YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_AGAINST_THE_CLAN_S1(1678),
	// Message: Only the creator of a channel can issue a global command.
	ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_ISSUE_A_GLOBAL_COMMAND(1679),
	// Message: $s1 has declined the channel invitation.
	S1_HAS_DECLINED_THE_CHANNEL_INVITATION(1680),
	// Message: Since $s1 did not respond, your channel invitation has failed.
	SINCE_S1_DID_NOT_RESPOND_YOUR_CHANNEL_INVITATION_HAS_FAILED(1681),
	// Message: Only the creator of a channel can use the channel dismiss command.
	ONLY_THE_CREATOR_OF_A_CHANNEL_CAN_USE_THE_CHANNEL_DISMISS_COMMAND(1682),
	// Message: Only a party leader can choose the option to leave a channel.
	ONLY_A_PARTY_LEADER_CAN_CHOOSE_THE_OPTION_TO_LEAVE_A_CHANNEL(1683),
	// Message: A Clan War can not be declared against a clan that is being dissolved.
	A_CLAN_WAR_CAN_NOT_BE_DECLARED_AGAINST_A_CLAN_THAT_IS_BEING_DISSOLVED(1684),
	// Message: You are unable to equip this item when your PK count is greater than or equal to one.
	YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE(1685),
	// Message: Stones and mortar tumble to the earth - the castle wall has taken damage!
	STONES_AND_MORTAR_TUMBLE_TO_THE_EARTH__THE_CASTLE_WALL_HAS_TAKEN_DAMAGE(1686),
	// Message: This area cannot be entered while mounted atop of a Wyvern.  You will be dismounted from your Wyvern if you do not leave!
	THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN__YOU_WILL_BE_DISMOUNTED_FROM_YOUR_WYVERN_IF_YOU_DO_NOT_LEAVE(1687),
	// Message: You cannot enchant while operating a Private Store or Private Workshop.
	YOU_CANNOT_ENCHANT_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP(1688),
	// Message: You have already joined the waiting list for a class specific match.
	YOU_HAVE_ALREADY_JOINED_THE_WAITING_LIST_FOR_A_CLASS_SPECIFIC_MATCH(1689),
	// Message: You have already joined the waiting list for a non-class specific match.
	YOU_HAVE_ALREADY_JOINED_THE_WAITING_LIST_FOR_A_NONCLASS_SPECIFIC_MATCH(1690),
	// Message: You can't join a Grand Olympiad Game match with that much stuff on you!  Reduce your weight to below 80 percent full and request to join again!
	YOU_CANT_JOIN_A_GRAND_OLYMPIAD_GAME_MATCH_WITH_THAT_MUCH_STUFF_ON_YOU__REDUCE_YOUR_WEIGHT_TO_BELOW_80_PERCENT_FULL_AND_REQUEST_TO_JOIN_AGAIN(1691),
	// Message: You have changed from your main class to a subclass and therefore are removed from the Grand Olympiad Games waiting list.
	YOU_HAVE_CHANGED_FROM_YOUR_MAIN_CLASS_TO_A_SUBCLASS_AND_THEREFORE_ARE_REMOVED_FROM_THE_GRAND_OLYMPIAD_GAMES_WAITING_LIST(1692),
	// Message: You may not observe a Grand Olympiad Games match while you are on the waiting list.
	YOU_MAY_NOT_OBSERVE_A_GRAND_OLYMPIAD_GAMES_MATCH_WHILE_YOU_ARE_ON_THE_WAITING_LIST(1693),
	// Message: Only a clan leader that is a Noblesse can view the Siege War Status window during a siege war.
	ONLY_A_CLAN_LEADER_THAT_IS_A_NOBLESSE_CAN_VIEW_THE_SIEGE_WAR_STATUS_WINDOW_DURING_A_SIEGE_WAR(1694),
	// Message: You can only use that during a Siege War!
	YOU_CAN_ONLY_USE_THAT_DURING_A_SIEGE_WAR(1695),
	// Message: A penalty will be imposed if your accumulated online access time is $s1 or greater.  Please consider taking a break.  Get some fresh air, eat some food or just simply relax.
	A_PENALTY_WILL_BE_IMPOSED_IF_YOUR_ACCUMULATED_ONLINE_ACCESS_TIME_IS_S1_OR_GREATER__PLEASE_CONSIDER_TAKING_A_BREAK__GET_SOME_FRESH_AIR_EAT_SOME_FOOD_OR_JUST_SIMPLY_RELAX(1696),
	// Message: Your cumulative online access time has exceeded $s1, so you will receive experience or item drops at 50 percent of the normal rate.  Please consider taking a break to bring your experience and item drop rates back to normal.
	YOUR_CUMULATIVE_ONLINE_ACCESS_TIME_HAS_EXCEEDED_S1_SO_YOU_WILL_RECEIVE_EXPERIENCE_OR_ITEM_DROPS_AT_50_PERCENT_OF_THE_NORMAL_RATE__PLEASE_CONSIDER_TAKING_A_BREAK_TO_BRING_YOUR_EXPERIENCE_AND_ITEM_DROP_RATES_BACK_TO_NORMAL(1697),
	// Message: Your cumulative online access time has exceeded $s1, so you will no longer receive experience or item drops.  Please consider taking a break to bring your experience and item drop rates back to normal.
	YOUR_CUMULATIVE_ONLINE_ACCESS_TIME_HAS_EXCEEDED_S1_SO_YOU_WILL_NO_LONGER_RECEIVE_EXPERIENCE_OR_ITEM_DROPS__PLEASE_CONSIDER_TAKING_A_BREAK_TO_BRING_YOUR_EXPERIENCE_AND_ITEM_DROP_RATES_BACK_TO_NORMAL(1698),
	// Message: You cannot dismiss a party member by force.
	YOU_CANNOT_DISMISS_A_PARTY_MEMBER_BY_FORCE(1699),
	// Message: You don't have enough spiritshots needed for a pet/servitor.
	YOU_DONT_HAVE_ENOUGH_SPIRITSHOTS_NEEDED_FOR_A_PETSERVITOR(1700),
	// Message: You don't have enough soulshots needed for a pet/servitor.
	YOU_DONT_HAVE_ENOUGH_SOULSHOTS_NEEDED_FOR_A_PETSERVITOR(1701),
	// Message: $s1 is using a third party program.
	S1_IS_USING_A_THIRD_PARTY_PROGRAM(1702),
	// Message: The previously investigated user is not using a third party program.
	THE_PREVIOUSLY_INVESTIGATED_USER_IS_NOT_USING_A_THIRD_PARTY_PROGRAM(1703),
	// Message: Please close the the setup window for your private manufacturing store or private store, and try again.
	PLEASE_CLOSE_THE_THE_SETUP_WINDOW_FOR_YOUR_PRIVATE_MANUFACTURING_STORE_OR_PRIVATE_STORE_AND_TRY_AGAIN(1704),
	// Message: PC Bang Points acquisition period. Points acquisition period left $s1 hour.
	PC_BANG_POINTS_ACQUISITION_PERIOD_POINTS_ACQUISITION_PERIOD_LEFT_S1_HOUR(1705),
	// Message: PC Bang Points use period. Points use period left $s1 hour.
	PC_BANG_POINTS_USE_PERIOD_POINTS_USE_PERIOD_LEFT_S1_HOUR(1706),
	// Message: You acquired $s1 PC Bang Point.
	YOU_ACQUIRED_S1_PC_BANG_POINT(1707),
	// Message: Double points! You acquired $s1 PC Bang Point.
	DOUBLE_POINTS_YOU_ACQUIRED_S1_PC_BANG_POINT(1708),
	// Message: You are using $s1 point.
	YOU_ARE_USING_S1_POINT(1709),
	// Message: You are short of accumulated points.
	YOU_ARE_SHORT_OF_ACCUMULATED_POINTS(1710),
	// Message: PC Bang Points use period has expired.
	PC_BANG_POINTS_USE_PERIOD_HAS_EXPIRED(1711),
	// Message: The PC Bang Points accumulation period has expired.
	THE_PC_BANG_POINTS_ACCUMULATION_PERIOD_HAS_EXPIRED(1712),
	// Message: The games may be delayed due to an insufficient number of players waiting.
	THE_GAMES_MAY_BE_DELAYED_DUE_TO_AN_INSUFFICIENT_NUMBER_OF_PLAYERS_WAITING(1713),
	// Message: Current Location: $s1, $s2, $s3 (Near the Town of Schuttgart)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_TOWN_OF_SCHUTTGART(1714),
	// Message: This is a Peaceful Zone\\n- PvP is not allowed in this area.
	THIS_IS_A_PEACEFUL_ZONEN_PVP_IS_NOT_ALLOWED_IN_THIS_AREA(1715),
	// Message: Altered Zone
	ALTERED_ZONE(1716),
	// Message: Siege War Zone \\n- A siege is currently in progress in this area.  \\n If a character dies in this zone, their resurrection ability may be restricted.
	SIEGE_WAR_ZONE_N_A_SIEGE_IS_CURRENTLY_IN_PROGRESS_IN_THIS_AREA__N_IF_A_CHARACTER_DIES_IN_THIS_ZONE_THEIR_RESURRECTION_ABILITY_MAY_BE_RESTRICTED(1717),
	// Message: General Field
	GENERAL_FIELD(1718),
	// Message: Seven Signs Zone \\n- Although a character's level may increase while in this area, HP and MP \\n will not be regenerated.
	SEVEN_SIGNS_ZONE_N_ALTHOUGH_A_CHARACTERS_LEVEL_MAY_INCREASE_WHILE_IN_THIS_AREA_HP_AND_MP_N_WILL_NOT_BE_REGENERATED(1719),
	// Message: ---
	//(1720),
	// Message: Combat Zone
	COMBAT_ZONE(1721),
	// Message: Please enter the name of the item you wish to search for.
	PLEASE_ENTER_THE_NAME_OF_THE_ITEM_YOU_WISH_TO_SEARCH_FOR(1722),
	// Message: Please take a moment to provide feedback about the petition service.
	PLEASE_TAKE_A_MOMENT_TO_PROVIDE_FEEDBACK_ABOUT_THE_PETITION_SERVICE(1723),
	// Message: A servitor whom is engaged in battle cannot be de-activated.
	A_SERVITOR_WHOM_IS_ENGAGED_IN_BATTLE_CANNOT_BE_DEACTIVATED(1724),
	// Message: You have earned $s1 raid point(s).
	YOU_HAVE_EARNED_S1_RAID_POINTS(1725),
	// Message: $s1 has disappeared since its allowed time period has expired.
	S1_HAS_DISAPPEARED_SINCE_ITS_ALLOWED_TIME_PERIOD_HAS_EXPIRED(1726),
	// Message: $s1 has invited you to a party room. Would you like to accept the invitation?
	S1_HAS_INVITED_YOU_TO_A_PARTY_ROOM_WOULD_YOU_LIKE_TO_ACCEPT_THE_INVITATION(1727),
	// Message: The recipient of your invitation did not accept the party matching invitation.
	THE_RECIPIENT_OF_YOUR_INVITATION_DID_NOT_ACCEPT_THE_PARTY_MATCHING_INVITATION(1728),
	// Message: You cannot join the guild party because the guild is currently teleporting.
	YOU_CANNOT_JOIN_THE_GUILD_PARTY_BECAUSE_THE_GUILD_IS_CURRENTLY_TELEPORTING(1729),
	// Message: To establish a Clan Academy, your clan must be Level 5 or higher.
	TO_ESTABLISH_A_CLAN_ACADEMY_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER(1730),
	// Message: Only the clan leader can create a Clan Academy.
	ONLY_THE_CLAN_LEADER_CAN_CREATE_A_CLAN_ACADEMY(1731),
	// Message: To create a Clan Academy, a Blood Mark is needed.
	TO_CREATE_A_CLAN_ACADEMY_A_BLOOD_MARK_IS_NEEDED(1732),
	// Message: You do not have enough adena to create a Clan Academy.
	YOU_DO_NOT_HAVE_ENOUGH_ADENA_TO_CREATE_A_CLAN_ACADEMY(1733),
	// Message: To join a Clan Academy, characters must be Level 40 or below, not belong another clan and not yet completed their 2nd class transfer.
	TO_JOIN_A_CLAN_ACADEMY_CHARACTERS_MUST_BE_LEVEL_40_OR_BELOW_NOT_BELONG_ANOTHER_CLAN_AND_NOT_YET_COMPLETED_THEIR_2ND_CLASS_TRANSFER(1734),
	// Message: $s1 does not meet the requirements to join a Clan Academy.
	S1_DOES_NOT_MEET_THE_REQUIREMENTS_TO_JOIN_A_CLAN_ACADEMY(1735),
	// Message: The Clan Academy has reached its maximum enrollment.
	THE_CLAN_ACADEMY_HAS_REACHED_ITS_MAXIMUM_ENROLLMENT(1736),
	// Message: Your clan has not established a Clan Academy but is eligible to do so.
	YOUR_CLAN_HAS_NOT_ESTABLISHED_A_CLAN_ACADEMY_BUT_IS_ELIGIBLE_TO_DO_SO(1737),
	// Message: Your clan has already established a Clan Academy.
	YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY(1738),
	// Message: Would you like to create a Clan Academy?
	WOULD_YOU_LIKE_TO_CREATE_A_CLAN_ACADEMY(1739),
	// Message: Please enter the name of the Clan Academy.
	PLEASE_ENTER_THE_NAME_OF_THE_CLAN_ACADEMY(1740),
	// Message: Congratulations! The $s1's Clan Academy has been created.
	CONGRATULATIONS_THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED(1741),
	// Message: A message inviting $s1 to join the Clan Academy is being sent.
	A_MESSAGE_INVITING_S1_TO_JOIN_THE_CLAN_ACADEMY_IS_BEING_SENT(1742),
	// Message: To open a Clan Academy, the leader of a Level 5 clan or above must pay XX Proofs of Blood or a certain amount of adena.
	TO_OPEN_A_CLAN_ACADEMY_THE_LEADER_OF_A_LEVEL_5_CLAN_OR_ABOVE_MUST_PAY_XX_PROOFS_OF_BLOOD_OR_A_CERTAIN_AMOUNT_OF_ADENA(1743),
	// Message: There was no response to your invitation to join the Clan Academy, so the invitation has been rescinded.
	THERE_WAS_NO_RESPONSE_TO_YOUR_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_SO_THE_INVITATION_HAS_BEEN_RESCINDED(1744),
	// Message: The recipient of your invitation to join the Clan Academy has declined.
	THE_RECIPIENT_OF_YOUR_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_HAS_DECLINED(1745),
	// Message: You have already joined a Clan Academy.
	YOU_HAVE_ALREADY_JOINED_A_CLAN_ACADEMY(1746),
	// Message: $s1 has sent you an invitation to join the Clan Academy belonging to the $s2 clan. Do you accept?
	S1_HAS_SENT_YOU_AN_INVITATION_TO_JOIN_THE_CLAN_ACADEMY_BELONGING_TO_THE_S2_CLAN_DO_YOU_ACCEPT(1747),
	// Message: Clan Academy member $s1 has successfully completed the 2nd class transfer and obtained $s2 Clan Reputation points.
	CLAN_ACADEMY_MEMBER_S1_HAS_SUCCESSFULLY_COMPLETED_THE_2ND_CLASS_TRANSFER_AND_OBTAINED_S2_CLAN_REPUTATION_POINTS(1748),
	// Message: Congratulations! You will now graduate from the Clan Academy and leave your current clan. As a graduate of the academy, you can immediately join a clan as a regular member without being subject to any penalties.
	CONGRATULATIONS_YOU_WILL_NOW_GRADUATE_FROM_THE_CLAN_ACADEMY_AND_LEAVE_YOUR_CURRENT_CLAN_AS_A_GRADUATE_OF_THE_ACADEMY_YOU_CAN_IMMEDIATELY_JOIN_A_CLAN_AS_A_REGULAR_MEMBER_WITHOUT_BEING_SUBJECT_TO_ANY_PENALTIES(1749),
	// Message: If you possess $s1, you cannot participate in the Olympiad.
	IF_YOU_POSSESS_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD(1750),
	// Message: The Grand Master has given you a commemorative item.
	THE_GRAND_MASTER_HAS_GIVEN_YOU_A_COMMEMORATIVE_ITEM(1751),
	// Message: Since the clan has received a graduate of the Clan Academy, it has earned $s1 points toward its reputation score.
	SINCE_THE_CLAN_HAS_RECEIVED_A_GRADUATE_OF_THE_CLAN_ACADEMY_IT_HAS_EARNED_S1_POINTS_TOWARD_ITS_REPUTATION_SCORE(1752),
	// Message: The clan leader has decreed that that particular privilege cannot be granted to a Clan Academy member.
	THE_CLAN_LEADER_HAS_DECREED_THAT_THAT_PARTICULAR_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER(1753),
	// Message: That privilege cannot be granted to a Clan Academy member.
	THAT_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER(1754),
	// Message: $s2 has been designated as the apprentice of clan member $s1.
	S2_HAS_BEEN_DESIGNATED_AS_THE_APPRENTICE_OF_CLAN_MEMBER_S1(1755),
	// Message: Your apprentice, $s1, has logged in.
	YOUR_APPRENTICE_S1_HAS_LOGGED_IN(1756),
	// Message: Your apprentice, $s1, has logged out.
	YOUR_APPRENTICE_S1_HAS_LOGGED_OUT(1757),
	// Message: Your sponsor, $s1, has logged in.
	YOUR_SPONSOR_S1_HAS_LOGGED_IN(1758),
	// Message: Your sponsor, $s1, has logged out.
	YOUR_SPONSOR_S1_HAS_LOGGED_OUT(1759),
	// Message: Clan member $s1's title has been changed to $s2.
	CLAN_MEMBER_S1S_TITLE_HAS_BEEN_CHANGED_TO_S2(1760),
	// Message: Clan member $s1's privilege level has been changed to $s2.
	CLAN_MEMBER_S1S_PRIVILEGE_LEVEL_HAS_BEEN_CHANGED_TO_S2(1761),
	// Message: You do not have the right to dismiss an apprentice.
	YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE(1762),
	// Message: $s2, clan member $s1's apprentice, has been removed.
	S2_CLAN_MEMBER_S1S_APPRENTICE_HAS_BEEN_REMOVED(1763),
	// Message: This item can only be worn by a member of the Clan Academy.
	THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY(1764),
	// Message: As a graduate of the Clan Academy, you can no longer wear this item.
	AS_A_GRADUATE_OF_THE_CLAN_ACADEMY_YOU_CAN_NO_LONGER_WEAR_THIS_ITEM(1765),
	// Message: An application to join the clan has been sent to $s1 in $s2.
	AN_APPLICATION_TO_JOIN_THE_CLAN_HAS_BEEN_SENT_TO_S1_IN_S2(1766),
	// Message: An application to join the Clan Academy has been sent to $s1.
	AN_APPLICATION_TO_JOIN_THE_CLAN_ACADEMY_HAS_BEEN_SENT_TO_S1(1767),
	// Message: $s1 has invited you to join the Clan Academy of $s2 clan. Would you like to join?
	S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_ACADEMY_OF_S2_CLAN_WOULD_YOU_LIKE_TO_JOIN(1768),
	// Message: $s1 has sent you an invitation to join the $s3 Order of Knights under the $s2 clan. Would you like to join?
	S1_HAS_SENT_YOU_AN_INVITATION_TO_JOIN_THE_S3_ORDER_OF_KNIGHTS_UNDER_THE_S2_CLAN_WOULD_YOU_LIKE_TO_JOIN(1769),
	// Message: The clan's reputation score has dropped below 0. The clan may face certain penalties as a result.
	THE_CLANS_REPUTATION_SCORE_HAS_DROPPED_BELOW_0_THE_CLAN_MAY_FACE_CERTAIN_PENALTIES_AS_A_RESULT(1770),
	// Message: Now that your clan level is above Level 5, it can accumulate clan reputation points.
	NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS(1771),
	// Message: Since your clan was defeated in a siege, $s1 points have been deducted from your clan's reputation score and given to the opposing clan.
	SINCE_YOUR_CLAN_WAS_DEFEATED_IN_A_SIEGE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLANS_REPUTATION_SCORE_AND_GIVEN_TO_THE_OPPOSING_CLAN(1772),
	// Message: Since your clan emerged victorious from the siege, $s1 points have been added to your clan's reputation score.
	SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1773),
	// Message: Your clan's newly acquired contested clan hall has added $s1 points to your clan's reputation score.
	YOUR_CLANS_NEWLY_ACQUIRED_CONTESTED_CLAN_HALL_HAS_ADDED_S1_POINTS_TO_YOUR_CLANS_REPUTATION_SCORE(1774),
	// Message: Clan member $s1 was an active member of the highest-ranked party in the Festival of Darkness. $s2 points have been added to your clan's reputation score.
	CLAN_MEMBER_S1_WAS_AN_ACTIVE_MEMBER_OF_THE_HIGHESTRANKED_PARTY_IN_THE_FESTIVAL_OF_DARKNESS_S2_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1775),
	// Message: Clan member $s1 was named a hero. $2s points have been added to your clan's reputation score.
	CLAN_MEMBER_S1_WAS_NAMED_A_HERO_2S_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1776),
	// Message: You have successfully completed a clan quest. $s1 points have been added to your clan's reputation score.
	YOU_HAVE_SUCCESSFULLY_COMPLETED_A_CLAN_QUEST_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE(1777),
	// Message: An opposing clan has captured your clan's contested clan hall. $s1 points have been deducted from your clan's reputation score.
	AN_OPPOSING_CLAN_HAS_CAPTURED_YOUR_CLANS_CONTESTED_CLAN_HALL_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLANS_REPUTATION_SCORE(1778),
	// Message: After losing the contested clan hall, 300 points have been deducted from your clan's reputation score.
	AFTER_LOSING_THE_CONTESTED_CLAN_HALL_300_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLANS_REPUTATION_SCORE(1779),
	// Message: Your clan has captured your opponent's contested clan hall. $s1 points have been deducted from your opponent's clan reputation score.
	YOUR_CLAN_HAS_CAPTURED_YOUR_OPPONENTS_CONTESTED_CLAN_HALL_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE(1780),
	// Message: Your clan has added $1s points to its clan reputation score.
	YOUR_CLAN_HAS_ADDED_1S_POINTS_TO_ITS_CLAN_REPUTATION_SCORE(1781),
	// Message: Your clan member $s1 was killed. $s2 points have been deducted from your clan's reputation score and added to your opponent's clan reputation score.
	YOUR_CLAN_MEMBER_S1_WAS_KILLED_S2_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLANS_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE(1782),
	// Message: For killing an opposing clan member, $s1 points have been deducted from your opponents' clan reputation score.
	FOR_KILLING_AN_OPPOSING_CLAN_MEMBER_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_OPPONENTS_CLAN_REPUTATION_SCORE(1783),
	// Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score and added to your opponents'.
	YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLANS_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS(1784),
	// Message: The clan you belong to has been initialized. $s1 points have been deducted from your clan reputation score.
	THE_CLAN_YOU_BELONG_TO_HAS_BEEN_INITIALIZED_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLAN_REPUTATION_SCORE(1785),
	// Message: Your clan has failed to defend the castle. $s1 points have been deducted from your clan's reputation score.
	YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOUR_CLANS_REPUTATION_SCORE(1786),
	// Message: $s1 points have been deducted from the clan's reputation score.
	S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_THE_CLANS_REPUTATION_SCORE(1787),
	// Message: The clan skill $s1 has been added.
	THE_CLAN_SKILL_S1_HAS_BEEN_ADDED(1788),
	// Message: Since the Clan Reputation Score has dropped to 0 or lower, your clan skill(s) will be de-activated.
	SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DEACTIVATED(1789),
	// Message: The conditions necessary to increase the clan's level have not been met.
	THE_CONDITIONS_NECESSARY_TO_INCREASE_THE_CLANS_LEVEL_HAVE_NOT_BEEN_MET(1790),
	// Message: The conditions necessary to create a military unit have not been met.
	THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET(1791),
	// Message: Please assign a manager for your new Order of Knights.
	PLEASE_ASSIGN_A_MANAGER_FOR_YOUR_NEW_ORDER_OF_KNIGHTS(1792),
	// Message: $s1 has been selected as the captain of $s2.
	S1_HAS_BEEN_SELECTED_AS_THE_CAPTAIN_OF_S2(1793),
	// Message: The Knights of $s1 have been created.
	THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED(1794),
	// Message: The Royal Guard of $s1 have been created.
	THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED(1795),
	// Message: This account has been temporarily suspended due to involvement in account theft or other abnormal game play which has harmed or inconvenienced other players.  If you were not involved with any of these violations, please contact Customer Service to verify your identity.  For more details, please visit the 1:1 Inquiry section of the Customer Support page at the official Lineage II website (www.lineage2.co.kr).
	THIS_ACCOUNT_HAS_BEEN_TEMPORARILY_SUSPENDED_DUE_TO_INVOLVEMENT_IN_ACCOUNT_THEFT_OR_OTHER_ABNORMAL_GAME_PLAY_WHICH_HAS_HARMED_OR_INCONVENIENCED_OTHER_PLAYERS__IF_YOU_WERE_NOT_INVOLVED_WITH_ANY_OF_THESE_VIOLATIONS_PLEASE_CONTACT_CUSTOMER_SERVICE_TO_VERIFY_YOUR_IDENTITY__FOR_MORE_DETAILS_PLEASE_VISIT_THE_11_INQUIRY_SECTION_OF_THE_CUSTOMER_SUPPORT_PAGE_AT_THE_OFFICIAL_LINEAGE_II_WEBSITE_WWWLINEAGE2COKR(1796),
	// Message: $s1 has been promoted to $s2.
	S1_HAS_BEEN_PROMOTED_TO_S2(1797),
	// Message: Clan lord privileges have been transferred to $s1.
	CLAN_LORD_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1(1798),
	// Message: We are searching for BOT users. Please try again later.
	WE_ARE_SEARCHING_FOR_BOT_USERS_PLEASE_TRY_AGAIN_LATER(1799),
	// Message: User $s1 has a history of using BOT.
	USER_S1_HAS_A_HISTORY_OF_USING_BOT(1800),
	// Message: The attempt to sell has failed.
	THE_ATTEMPT_TO_SELL_HAS_FAILED(1801),
	// Message: The attempt to trade has failed.
	THE_ATTEMPT_TO_TRADE_HAS_FAILED(1802),
	// Message: The request to participate in the game cannot be made starting from 10 minutes before the end of the game.
	THE_REQUEST_TO_PARTICIPATE_IN_THE_GAME_CANNOT_BE_MADE_STARTING_FROM_10_MINUTES_BEFORE_THE_END_OF_THE_GAME(1803),
	// Message: This account has been suspended for 7 days.
	THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_7_DAYS(1804),
	// Message: This account has been suspended for 30 days.
	THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_30_DAYS(1805),
	// Message: This account has been permanently banned.
	THIS_ACCOUNT_HAS_BEEN_PERMANENTLY_BANNED(1806),
	// Message: This account has been suspended for 30 days because you have engaged in a cash transaction. For more details, please visit the support section of the official Lineage II website (http://support.plaync.com).
	THIS_ACCOUNT_HAS_BEEN_SUSPENDED_FOR_30_DAYS_BECAUSE_YOU_HAVE_ENGAGED_IN_A_CASH_TRANSACTION_FOR_MORE_DETAILS_PLEASE_VISIT_THE_SUPPORT_SECTION_OF_THE_OFFICIAL_LINEAGE_II_WEBSITE_HTTPSUPPORTPLAYNCCOM(1807),
	// Message: This account has been permanently banned.
	THIS_ACCOUNT_HAS_BEEN_PERMANENTLY_BANNED_(1808),
	// Message: Account owner must be verified in order to use this account again.
	ACCOUNT_OWNER_MUST_BE_VERIFIED_IN_ORDER_TO_USE_THIS_ACCOUNT_AGAIN(1809),
	// Message: The refuse invitation state has been activated.
	THE_REFUSE_INVITATION_STATE_HAS_BEEN_ACTIVATED(1810),
	// Message: The refuse invitation state has been removed.
	THE_REFUSE_INVITATION_STATE_HAS_BEEN_REMOVED(1811),
	// Message: Since the refuse invitation state is currently activated, no invitation can be made.
	SINCE_THE_REFUSE_INVITATION_STATE_IS_CURRENTLY_ACTIVATED_NO_INVITATION_CAN_BE_MADE(1812),
	// Message: $s1 has $s2 hour(s) of usage time remaining.
	S1_HAS_S2_HOURS_OF_USAGE_TIME_REMAINING(1813),
	// Message: $s1 has $s2 minute(s) of usage time remaining.
	S1_HAS_S2_MINUTES_OF_USAGE_TIME_REMAINING(1814),
	// Message: $s2 was dropped in the $s1 region.
	S2_WAS_DROPPED_IN_THE_S1_REGION(1815),
	// Message: The owner of $s2 has appeared in the $s1 region.
	THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION(1816),
	// Message: $s2's owner has logged into the $s1 region.
	S2S_OWNER_HAS_LOGGED_INTO_THE_S1_REGION(1817),
	// Message: $s1 has disappeared.
	S1_HAS_DISAPPEARED_(1818),
	// Message: An evil is pulsating from $s2 in $s1.
	AN_EVIL_IS_PULSATING_FROM_S2_IN_S1(1819),
	// Message: $s1 is currently asleep.
	S1_IS_CURRENTLY_ASLEEP(1820),
	// Message: $s2's evil presence is felt in $s1.
	S2S_EVIL_PRESENCE_IS_FELT_IN_S1(1821),
	// Message: $s1 has been sealed.
	S1_HAS_BEEN_SEALED(1822),
	// Message: The registration period for a clan hall war has ended.
	THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED(1823),
	// Message: You have been registered for a clan hall war.  Please move to the left side of the clan hall's arena and get ready.
	YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR__PLEASE_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA_AND_GET_READY(1824),
	// Message: You have failed in your attempt to register for the clan hall war. Please try again.
	YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR_PLEASE_TRY_AGAIN(1825),
	// Message: In $s1 minute(s), the game will begin. All players must hurry and move to the left side of the clan hall's arena.
	IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA(1826),
	// Message: In $s1 minute(s), the game will begin. All players, please enter the arena now.
	IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW(1827),
	// Message: In $s1 second(s), the game will begin.
	IN_S1_SECONDS_THE_GAME_WILL_BEGIN(1828),
	// Message: Since the number of parties in the guild has exceeded the maximum, no new party may participate at this time.
	SINCE_THE_NUMBER_OF_PARTIES_IN_THE_GUILD_HAS_EXCEEDED_THE_MAXIMUM_NO_NEW_PARTY_MAY_PARTICIPATE_AT_THIS_TIME(1829),
	// Message: $s1 is not allowed to use the party room invite command. Please update the waiting list.
	S1_IS_NOT_ALLOWED_TO_USE_THE_PARTY_ROOM_INVITE_COMMAND_PLEASE_UPDATE_THE_WAITING_LIST(1830),
	// Message: $s1 does not meet the conditions of the party room. Please update the waiting list.
	S1_DOES_NOT_MEET_THE_CONDITIONS_OF_THE_PARTY_ROOM_PLEASE_UPDATE_THE_WAITING_LIST(1831),
	// Message: Only a room leader may invite others to a party room.
	ONLY_A_ROOM_LEADER_MAY_INVITE_OTHERS_TO_A_PARTY_ROOM(1832),
	// Message: All of $s1 will be dropped. Would you like to continue?
	ALL_OF_S1_WILL_BE_DROPPED_WOULD_YOU_LIKE_TO_CONTINUE(1833),
	// Message: The party room is full. No more characters can be invited in.
	THE_PARTY_ROOM_IS_FULL_NO_MORE_CHARACTERS_CAN_BE_INVITED_IN(1834),
	// Message: $s1 is full and cannot accept additional clan members at this time.
	S1_IS_FULL_AND_CANNOT_ACCEPT_ADDITIONAL_CLAN_MEMBERS_AT_THIS_TIME(1835),
	// Message: You cannot join a Clan Academy because you have successfully completed your 2nd class transfer.
	YOU_CANNOT_JOIN_A_CLAN_ACADEMY_BECAUSE_YOU_HAVE_SUCCESSFULLY_COMPLETED_YOUR_2ND_CLASS_TRANSFER(1836),
	// Message: $s1 has sent you an invitation to join the $s3 Royal Guard under the $s2 clan. Would you like to join?
	S1_HAS_SENT_YOU_AN_INVITATION_TO_JOIN_THE_S3_ROYAL_GUARD_UNDER_THE_S2_CLAN_WOULD_YOU_LIKE_TO_JOIN(1837),
	// Message: 1. The coupon can be used once per character.
	_1_THE_COUPON_CAN_BE_USED_ONCE_PER_CHARACTER(1838),
	// Message: 2. A used serial number may not be used again.
	_2_A_USED_SERIAL_NUMBER_MAY_NOT_BE_USED_AGAIN(1839),
	// Message: 3. If you enter the incorrect serial number more than 5 times,\\n   you may use it again after a certain amount of time passes.
	_3_IF_YOU_ENTER_THE_INCORRECT_SERIAL_NUMBER_MORE_THAN_5_TIMESN___YOU_MAY_USE_IT_AGAIN_AFTER_A_CERTAIN_AMOUNT_OF_TIME_PASSES(1840),
	// Message: This clan hall war has been cancelled.  Not enough clans have registered.
	THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED__NOT_ENOUGH_CLANS_HAVE_REGISTERED(1841),
	// Message: $s1 wishes to summon you from $s2. Do you accept?
	S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT(1842),
	// Message: $s1 is engaged in combat and cannot be summoned.
	S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED(1843),
	// Message: $s1 is dead at the moment and cannot be summoned.
	S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED(1844),
	// Message: Hero weapons cannot be destroyed.
	HERO_WEAPONS_CANNOT_BE_DESTROYED(1845),
	// Message: You are too far away from the Strider to mount it.
	YOU_ARE_TOO_FAR_AWAY_FROM_THE_STRIDER_TO_MOUNT_IT(1846),
	// Message: You caught a fish $s1 in length.
	YOU_CAUGHT_A_FISH_S1_IN_LENGTH(1847),
	// Message: Because of the size of fish caught, you will be registered in the ranking.
	BECAUSE_OF_THE_SIZE_OF_FISH_CAUGHT_YOU_WILL_BE_REGISTERED_IN_THE_RANKING(1848),
	// Message: All of $s1 will be discarded. Would you like to continue?
	ALL_OF_S1_WILL_BE_DISCARDED_WOULD_YOU_LIKE_TO_CONTINUE(1849),
	// Message: The Captain of the Order of Knights cannot be appointed.
	THE_CAPTAIN_OF_THE_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED(1850),
	// Message: The Captain of the Royal Guard cannot be appointed.
	THE_CAPTAIN_OF_THE_ROYAL_GUARD_CANNOT_BE_APPOINTED(1851),
	// Message: The attempt to acquire the skill has failed because of an insufficient Clan Reputation Score.
	THE_ATTEMPT_TO_ACQUIRE_THE_SKILL_HAS_FAILED_BECAUSE_OF_AN_INSUFFICIENT_CLAN_REPUTATION_SCORE(1852),
	// Message: Quantity items of the same type cannot be exchanged at the same time.
	QUANTITY_ITEMS_OF_THE_SAME_TYPE_CANNOT_BE_EXCHANGED_AT_THE_SAME_TIME(1853),
	// Message: The item was converted successfully.
	THE_ITEM_WAS_CONVERTED_SUCCESSFULLY(1854),
	// Message: Another military unit is already using that name. Please enter a different name.
	ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME(1855),
	// Message: Since your opponent is now the owner of $s1, the Olympiad has been cancelled.
	SINCE_YOUR_OPPONENT_IS_NOW_THE_OWNER_OF_S1_THE_OLYMPIAD_HAS_BEEN_CANCELLED(1856),
	// Message: Since you now own $s1, you cannot participate in the Olympiad.
	SINCE_YOU_NOW_OWN_S1_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD(1857),
	// Message: You cannot participate in the Olympiad while dead.
	YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_WHILE_DEAD(1858),
	// Message: You have exceeded the quantity that can be moved at one time.
	YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_MOVED_AT_ONE_TIME(1859),
	// Message: The Clan Reputation Score is too low.
	THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW(1860),
	// Message: The clan's crest has been deleted.
	THE_CLANS_CREST_HAS_BEEN_DELETED(1861),
	// Message: The clan skill will be activated because the clan's reputation score has reached to 0 or higher.
	THE_CLAN_SKILL_WILL_BE_ACTIVATED_BECAUSE_THE_CLANS_REPUTATION_SCORE_HAS_REACHED_TO_0_OR_HIGHER(1862),
	// Message: $s1 purchased a clan item, reducing the Clan Reputation by $s2 points.
	S1_PURCHASED_A_CLAN_ITEM_REDUCING_THE_CLAN_REPUTATION_BY_S2_POINTS(1863),
	// Message: Your pet/servitor is unresponsive and will not obey any orders.
	YOUR_PETSERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS(1864),
	// Message: Your pet/servitor is currently in a state of distress.
	YOUR_PETSERVITOR_IS_CURRENTLY_IN_A_STATE_OF_DISTRESS(1865),
	// Message: MP was reduced by $s1.
	MP_WAS_REDUCED_BY_S1(1866),
	// Message: Your opponent's MP was reduced by $s1.
	YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1(1867),
	// Message: You cannot exchange an item while it is being used.
	YOU_CANNOT_EXCHANGE_AN_ITEM_WHILE_IT_IS_BEING_USED(1868),
	// Message: $s1 has granted the guild's master party the privilege of item looting.
	S1_HAS_GRANTED_THE_GUILDS_MASTER_PARTY_THE_PRIVILEGE_OF_ITEM_LOOTING(1869),
	// Message: A command channel with the item looting privilege already exists.
	A_COMMAND_CHANNEL_WITH_THE_ITEM_LOOTING_PRIVILEGE_ALREADY_EXISTS(1870),
	// Message: Do you want to dismiss $s1 from the clan?
	DO_YOU_WANT_TO_DISMISS_S1_FROM_THE_CLAN(1871),
	// Message: You have $s1 hour(s) and $s2 minute(s) left.
	YOU_HAVE_S1_HOURS_AND_S2_MINUTES_LEFT(1872),
	// Message: There are $s1 hour(s) and $s2 minute(s) left in the fixed use time for this PC Café.
	THERE_ARE_S1_HOURS_AND_S2_MINUTES_LEFT_IN_THE_FIXED_USE_TIME_FOR_THIS_PC_CAF(1873),
	// Message: There are $s1 minute(s) left for this individual user.
	THERE_ARE_S1_MINUTES_LEFT_FOR_THIS_INDIVIDUAL_USER(1874),
	// Message: There are $s1 minute(s) left in the fixed use time for this PC Café.
	THERE_ARE_S1_MINUTES_LEFT_IN_THE_FIXED_USE_TIME_FOR_THIS_PC_CAF(1875),
	// Message: Do you want to leave $s1 clan?
	DO_YOU_WANT_TO_LEAVE_S1_CLAN(1876),
	// Message: The game will end in $s1 minute(s).
	THE_GAME_WILL_END_IN_S1_MINUTES(1877),
	// Message: The game will end in $s1 second(s).
	THE_GAME_WILL_END_IN_S1_SECONDS(1878),
	// Message: In $s1 minute(s), you will be teleported outside of the game arena.
	IN_S1_MINUTES_YOU_WILL_BE_TELEPORTED_OUTSIDE_OF_THE_GAME_ARENA(1879),
	// Message: In $s1 second(s), you will be teleported outside of the game arena.
	IN_S1_SECONDS_YOU_WILL_BE_TELEPORTED_OUTSIDE_OF_THE_GAME_ARENA(1880),
	// Message: The preliminary match will begin in $s1 second(s). Prepare yourself.
	THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS_PREPARE_YOURSELF(1881),
	// Message: Characters cannot be created from this server.
	CHARACTERS_CANNOT_BE_CREATED_FROM_THIS_SERVER(1882),
	// Message: There are no offerings I own or I made a bid for.
	THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR(1883),
	// Message: Enter the PC Room coupon serial number:
	ENTER_THE_PC_ROOM_COUPON_SERIAL_NUMBER(1884),
	// Message: This serial number cannot be entered. Please try again in $s1 minute(s).
	THIS_SERIAL_NUMBER_CANNOT_BE_ENTERED_PLEASE_TRY_AGAIN_IN_S1_MINUTES(1885),
	// Message: This serial number has already been used.
	THIS_SERIAL_NUMBER_HAS_ALREADY_BEEN_USED(1886),
	// Message: Invalid serial number. Your attempt to enter the number has failed $s1 time(s). You will be allowed to make $s2 more attempt(s).
	INVALID_SERIAL_NUMBER_YOUR_ATTEMPT_TO_ENTER_THE_NUMBER_HAS_FAILED_S1_TIMES_YOU_WILL_BE_ALLOWED_TO_MAKE_S2_MORE_ATTEMPTS(1887),
	// Message: Invalid serial number.  Your attempt to enter the number has failed 5 times.  Please try again in 4 hours.
	INVALID_SERIAL_NUMBER__YOUR_ATTEMPT_TO_ENTER_THE_NUMBER_HAS_FAILED_5_TIMES__PLEASE_TRY_AGAIN_IN_4_HOURS(1888),
	// Message: Congratulations. You have received $s1.
	CONGRATULATIONS_YOU_HAVE_RECEIVED_S1(1889),
	// Message: Since you have already used this coupon, you may not use this serial number.
	SINCE_YOU_HAVE_ALREADY_USED_THIS_COUPON_YOU_MAY_NOT_USE_THIS_SERIAL_NUMBER(1890),
	// Message: You may not use items in a private store or private work shop.
	YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP(1891),
	// Message: The replay file for the previous version cannot be played.
	THE_REPLAY_FILE_FOR_THE_PREVIOUS_VERSION_CANNOT_BE_PLAYED(1892),
	// Message: This file cannot be replayed.
	THIS_FILE_CANNOT_BE_REPLAYED(1893),
	// Message: A sub-class cannot be created or changed while you are over your weight limit.
	A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT(1894),
	// Message: $s1 is in an area which blocks summoning.
	S1_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING(1895),
	// Message: $s1 has already been summoned!
	S1_HAS_ALREADY_BEEN_SUMMONED(1896),
	// Message: $s1 is required for summoning.
	S1_IS_REQUIRED_FOR_SUMMONING(1897),
	// Message: $s1 is currently trading or operating a private store and cannot be summoned.
	S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED(1898),
	// Message: Your target is in an area which blocks summoning.
	YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING(1899),
	// Message: $s1 has entered the party room.
	S1_HAS_ENTERED_THE_PARTY_ROOM(1900),
	// Message: $s1 has invited you to enter the party room.
	S1_HAS_INVITED_YOU_TO_ENTER_THE_PARTY_ROOM(1901),
	// Message: Incompatible item grade.  This item cannot be used.
	INCOMPATIBLE_ITEM_GRADE__THIS_ITEM_CANNOT_BE_USED(1902),
	// Message: Those of you who have requested NCOTP should run NCOTP \\n by using your cell phone to get the NCOTP \\n password and enter it within 1 minute.\\n  If you have not requested NCOTP, leave this field blank and\\n click the Login button.
	THOSE_OF_YOU_WHO_HAVE_REQUESTED_NCOTP_SHOULD_RUN_NCOTP_N_BY_USING_YOUR_CELL_PHONE_TO_GET_THE_NCOTP_N_PASSWORD_AND_ENTER_IT_WITHIN_1_MINUTEN__IF_YOU_HAVE_NOT_REQUESTED_NCOTP_LEAVE_THIS_FIELD_BLANK_ANDN_CLICK_THE_LOGIN_BUTTON(1903),
	// Message: A sub-class may not be created or changed while a servitor or pet is summoned.
	A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED(1904),
	// Message: $s2 of $s1 will be replaced with $s4 of $s3.
	S2_OF_S1_WILL_BE_REPLACED_WITH_S4_OF_S3(1905),
	// Message: Select the combat unit\\n to transfer to.
	SELECT_THE_COMBAT_UNITN_TO_TRANSFER_TO(1906),
	// Message: Select the the character who will\\n replace the current character.
	SELECT_THE_THE_CHARACTER_WHO_WILLN_REPLACE_THE_CURRENT_CHARACTER(1907),
	// Message: $s1 is in a state which prevents summoning.
	S1_IS_IN_A_STATE_WHICH_PREVENTS_SUMMONING(1908),
	// Message: ==< List of Clan Academy Graduates During the Past Week >==
	_LIST_OF_CLAN_ACADEMY_GRADUATES_DURING_THE_PAST_WEEK_(1909),
	// Message: Graduates:  $s1
	GRADUATES__S1(1910),
	// Message: You cannot summon players who are currently participating in the Grand Olympiad.
	YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD(1911),
	// Message: Only those requesting NCOTP should make an entry into this field.
	ONLY_THOSE_REQUESTING_NCOTP_SHOULD_MAKE_AN_ENTRY_INTO_THIS_FIELD(1912),
	// Message: The remaining recycle time for $s1 is $s2 minute(s).
	THE_REMAINING_RECYCLE_TIME_FOR_S1_IS_S2_MINUTES(1913),
	// Message: The remaining recycle time for $s1 is $s2 second(s).
	THE_REMAINING_RECYCLE_TIME_FOR_S1_IS_S2_SECONDS(1914),
	// Message: The game will end in $s1 second(s).
	THE_GAME_WILL_END_IN_S1_SECONDS_(1915),
	// Message: Your Death Penalty is now level $s1.
	YOUR_DEATH_PENALTY_IS_NOW_LEVEL_S1(1916),
	// Message: Your Death Penalty has been lifted.
	YOUR_DEATH_PENALTY_HAS_BEEN_LIFTED(1917),
	// Message: Your pet is too high level to control.
	YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL(1918),
	// Message: The Grand Olympiad registration period has ended.
	THE_GRAND_OLYMPIAD_REGISTRATION_PERIOD_HAS_ENDED(1919),
	// Message: Your account is currently inactive. If you do not log into the game for a certain period of time, your account will then be changed into an inactive account.  You can reactivate your account by visiting the Lineage II home page (www.lineage2.co.kr).
	YOUR_ACCOUNT_IS_CURRENTLY_INACTIVE_IF_YOU_DO_NOT_LOG_INTO_THE_GAME_FOR_A_CERTAIN_PERIOD_OF_TIME_YOUR_ACCOUNT_WILL_THEN_BE_CHANGED_INTO_AN_INACTIVE_ACCOUNT__YOU_CAN_REACTIVATE_YOUR_ACCOUNT_BY_VISITING_THE_LINEAGE_II_HOME_PAGE_WWWLINEAGE2COKR(1920),
	// Message: $s2 hour(s) and $s3 minute(s) have passed since $s1 has killed.
	S2_HOURS_AND_S3_MINUTES_HAVE_PASSED_SINCE_S1_HAS_KILLED(1921),
	// Message: Because $s1 failed to kill for one full day, it has expired.
	BECAUSE_S1_FAILED_TO_KILL_FOR_ONE_FULL_DAY_IT_HAS_EXPIRED(1922),
	// Message: Court Magician: The portal has been created!
	COURT_MAGICIAN_THE_PORTAL_HAS_BEEN_CREATED(1923),
	// Message: Current Location: $s1, $s2, $s3 (near the Primeval Isle)
	CURRENT_LOCATION_S1_S2_S3_NEAR_THE_PRIMEVAL_ISLE(1924),
	// Message: Due to the affects of the Seal of Strife, it is not possible to summon at this time.
	DUE_TO_THE_AFFECTS_OF_THE_SEAL_OF_STRIFE_IT_IS_NOT_POSSIBLE_TO_SUMMON_AT_THIS_TIME(1925),
	// Message: There is no opponent to receive your challenge for a duel.
	THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL(1926),
	// Message: $s1 has been challenged to a duel.
	S1_HAS_BEEN_CHALLENGED_TO_A_DUEL(1927),
	// Message: $s1's party has been challenged to a  duel.
	S1S_PARTY_HAS_BEEN_CHALLENGED_TO_A__DUEL(1928),
	// Message: $s1 has accepted your challenge to a duel. The duel will begin in a few moments.
	S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS(1929),
	// Message: You have accepted $s1's challenge to a duel. The duel will begin in a few moments.
	YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS(1930),
	// Message: $s1 has declined your challenge to a duel.
	S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL(1931),
	// Message: $s1 has declined your challenge to a duel.
	S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL_(1932),
	// Message: You have accepted $s1's challenge to a party duel. The duel will begin in a few moments.
	YOU_HAVE_ACCEPTED_S1S_CHALLENGE_TO_A_PARTY_DUEL_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS(1933),
	// Message: $s1 has accepted your challenge to duel against their party. The duel will begin in a few moments.
	S1_HAS_ACCEPTED_YOUR_CHALLENGE_TO_DUEL_AGAINST_THEIR_PARTY_THE_DUEL_WILL_BEGIN_IN_A_FEW_MOMENTS(1934),
	// Message: $s1 has declined your challenge to a duel.
	S1_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL__(1935),
	// Message: The opposing party has declined your challenge to a duel.
	THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL(1936),
	// Message: Since the person you challenged is not currently in a party, they cannot duel against your party.
	SINCE_THE_PERSON_YOU_CHALLENGED_IS_NOT_CURRENTLY_IN_A_PARTY_THEY_CANNOT_DUEL_AGAINST_YOUR_PARTY(1937),
	// Message: $s1 has challenged you to a duel.
	S1_HAS_CHALLENGED_YOU_TO_A_DUEL(1938),
	// Message: $s1's party has challenged your party to a duel.
	S1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL(1939),
	// Message: You are unable to request a duel at this time.
	YOU_ARE_UNABLE_TO_REQUEST_A_DUEL_AT_THIS_TIME(1940),
	// Message: This is not a suitable place to challenge anyone or party to a duel.
	THIS_IS_NOT_A_SUITABLE_PLACE_TO_CHALLENGE_ANYONE_OR_PARTY_TO_A_DUEL(1941),
	// Message: The opposing party is currently unable to accept a challenge to a duel.
	THE_OPPOSING_PARTY_IS_CURRENTLY_UNABLE_TO_ACCEPT_A_CHALLENGE_TO_A_DUEL(1942),
	// Message: The opposing party is currently not in a suitable location for a duel.
	THE_OPPOSING_PARTY_IS_CURRENTLY_NOT_IN_A_SUITABLE_LOCATION_FOR_A_DUEL(1943),
	// Message: In a moment, you will be transported to the site where the duel will take place.
	IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE(1944),
	// Message: The duel will begin in $s1 second(s).
	THE_DUEL_WILL_BEGIN_IN_S1_SECONDS(1945),
	// Message: $s1 has challenged you to a duel. Will you accept?
	S1_HAS_CHALLENGED_YOU_TO_A_DUEL_WILL_YOU_ACCEPT(1946),
	// Message: $s1's party has challenged your party to a duel. Will you accept?
	S1S_PARTY_HAS_CHALLENGED_YOUR_PARTY_TO_A_DUEL_WILL_YOU_ACCEPT(1947),
	// Message: The duel will begin in $s1 second(s).
	THE_DUEL_WILL_BEGIN_IN_S1_SECONDS_(1948),
	// Message: Let the duel begin!
	LET_THE_DUEL_BEGIN(1949),
	// Message: $s1 has won the duel.
	S1_HAS_WON_THE_DUEL(1950),
	// Message: $s1's party has won the duel.
	S1S_PARTY_HAS_WON_THE_DUEL(1951),
	// Message: The duel has ended in a tie.
	THE_DUEL_HAS_ENDED_IN_A_TIE(1952),
	// Message: Since $s1 was disqualified, $s2 has won.
	SINCE_S1_WAS_DISQUALIFIED_S2_HAS_WON(1953),
	// Message: Since $s1's party was disqualified, $s2's party has won.
	SINCE_S1S_PARTY_WAS_DISQUALIFIED_S2S_PARTY_HAS_WON(1954),
	// Message: Since $s1 withdrew from the duel, $s2 has won.
	SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON(1955),
	// Message: Since $s1's party withdrew from the duel, $s2's party has won.
	SINCE_S1S_PARTY_WITHDREW_FROM_THE_DUEL_S2S_PARTY_HAS_WON(1956),
	// Message: Select the item to be augmented.
	SELECT_THE_ITEM_TO_BE_AUGMENTED(1957),
	// Message: Select the catalyst for augmentation.
	SELECT_THE_CATALYST_FOR_AUGMENTATION(1958),
	// Message: You have placed $s2 $s1(s).
	YOU_HAVE_PLACED_S2_S1S(1959),
	// Message: This is not a suitable item.
	THIS_IS_NOT_A_SUITABLE_ITEM(1960),
	// Message: Gemstone quantity is incorrect.
	GEMSTONE_QUANTITY_IS_INCORRECT(1961),
	// Message: The item was successfully augmented!
	THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED(1962),
	// Message: Select the item from which you wish to remove augmentation.
	SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION(1963),
	// Message: Augmentation removal can only be done on an augmented item.
	AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM(1964),
	// Message: Augmentation has been successfully removed from your $s1.
	AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1(1965),
	// Message: Only the clan leader may issue commands.
	ONLY_THE_CLAN_LEADER_MAY_ISSUE_COMMANDS(1966),
	// Message: The gate is firmly locked. Please try again later.
	THE_GATE_IS_FIRMLY_LOCKED_PLEASE_TRY_AGAIN_LATER(1967),
	// Message: $s1's owner.
	S1S_OWNER(1968),
	// Message: Area where $s1 appears.
	AREA_WHERE_S1_APPEARS(1969),
	// Message: Once an item is augmented, it cannot be augmented again.
	ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN(1970),
	// Message: The level of the hardener is too high to be used.
	THE_LEVEL_OF_THE_HARDENER_IS_TOO_HIGH_TO_BE_USED(1971),
	// Message: You cannot augment items while a private store or private workshop is in operation.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP_IS_IN_OPERATION(1972),
	// Message: You cannot augment items while frozen.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_FROZEN(1973),
	// Message: You cannot augment items while dead.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_DEAD(1974),
	// Message: You cannot augment items while engaged in trade activities.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_ENGAGED_IN_TRADE_ACTIVITIES(1975),
	// Message: You cannot augment items while paralyzed.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_PARALYZED(1976),
	// Message: You cannot augment items while fishing.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_FISHING(1977),
	// Message: You cannot augment items while sitting down.
	YOU_CANNOT_AUGMENT_ITEMS_WHILE_SITTING_DOWN(1978),
	// Message: $s1's remaining M. Atk. is now 10.
	S1S_REMAINING_M_ATK_IS_NOW_10(1979),
	// Message: $s1's remaining M. Atk. is now 5.
	S1S_REMAINING_M_ATK_IS_NOW_5(1980),
	// Message: $s1's remaining M. Atk. is now 1. It will disappear soon.
	S1S_REMAINING_M_ATK_IS_NOW_1_IT_WILL_DISAPPEAR_SOON(1981),
	// Message: $s1's remaining M. Atk. is now 0, and the item has disappeared.
	S1S_REMAINING_M_ATK_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED(1982),
	// Message: $s1
	S1_(1983),
	// Message: Press the Augment button to begin.
	PRESS_THE_AUGMENT_BUTTON_TO_BEGIN(1984),
	// Message: $s1's drop area ($s2)
	S1S_DROP_AREA_S2(1985),
	// Message: $s1's owner ($s2)
	S1S_OWNER_S2(1986),
	// Message: $s1
	S1__(1987),
	// Message: The ferry has arrived at Primeval Isle.
	THE_FERRY_HAS_ARRIVED_AT_PRIMEVAL_ISLE(1988),
	// Message: The ferry will leave for Rune Harbor after anchoring for three minutes.
	THE_FERRY_WILL_LEAVE_FOR_RUNE_HARBOR_AFTER_ANCHORING_FOR_THREE_MINUTES(1989),
	// Message: The ferry is now departing Primeval Isle for Rune Harbor.
	THE_FERRY_IS_NOW_DEPARTING_PRIMEVAL_ISLE_FOR_RUNE_HARBOR(1990),
	// Message: The ferry will leave for Primeval Isle after anchoring for three minutes.
	THE_FERRY_WILL_LEAVE_FOR_PRIMEVAL_ISLE_AFTER_ANCHORING_FOR_THREE_MINUTES(1991),
	// Message: The ferry is now departing Rune Harbor for Primeval Isle.
	THE_FERRY_IS_NOW_DEPARTING_RUNE_HARBOR_FOR_PRIMEVAL_ISLE(1992),
	// Message: The ferry from Primeval Isle to Rune Harbor has been delayed.
	THE_FERRY_FROM_PRIMEVAL_ISLE_TO_RUNE_HARBOR_HAS_BEEN_DELAYED(1993),
	// Message: The ferry from Rune Harbor to Primeval Isle has been delayed.
	THE_FERRY_FROM_RUNE_HARBOR_TO_PRIMEVAL_ISLE_HAS_BEEN_DELAYED(1994),
	// Message: $s1 channel filtering option
	S1_CHANNEL_FILTERING_OPTION(1995),
	// Message: The target is currently not registered.
	THE_TARGET_IS_CURRENTLY_NOT_REGISTERED(1996),
	// Message: $s1 is performing a counter-attack.
	S1_IS_PERFORMING_A_COUNTERATTACK(1997),
	// Message: You counter-attack $s1's attack.
	YOU_COUNTERATTACK_S1S_ATTACK(1998),
	// Message: $s1 dodges the attack.
	S1_DODGES_THE_ATTACK(1999),
	// Message: You dodge $s1's attack.
	YOU_DODGE_S1S_ATTACK(2000),
	// Message: Augmentation failed because it did not go through the normal augmentation process.
	AUGMENTATION_FAILED_BECAUSE_IT_DID_NOT_GO_THROUGH_THE_NORMAL_AUGMENTATION_PROCESS(2001),
	// Message: Trap failed.
	TRAP_FAILED(2002),
	// Message: You obtained an ordinary material.
	YOU_OBTAINED_AN_ORDINARY_MATERIAL(2003),
	// Message: You obtained a rare material.
	YOU_OBTAINED_A_RARE_MATERIAL(2004),
	// Message: You obtained a unique material.
	YOU_OBTAINED_A_UNIQUE_MATERIAL(2005),
	// Message: You obtained the only material of this kind.
	YOU_OBTAINED_THE_ONLY_MATERIAL_OF_THIS_KIND(2006),
	// Message: Please enter the recipient's name.
	PLEASE_ENTER_THE_RECIPIENTS_NAME(2007),
	// Message: Please enter the text.
	PLEASE_ENTER_THE_TEXT(2008),
	// Message: You cannot exceed 1500 characters.
	YOU_CANNOT_EXCEED_1500_CHARACTERS(2009),
	// Message: $s2 $s1
	S2_S1(2010),
	// Message: The augmented item cannot be discarded.
	THE_AUGMENTED_ITEM_CANNOT_BE_DISCARDED(2011),
	// Message: $s1 has been moved (activated).
	S1_HAS_BEEN_MOVED_ACTIVATED(2012),
	// Message: Your seed or remaining purchase amount is inadequate.
	YOUR_SEED_OR_REMAINING_PURCHASE_AMOUNT_IS_INADEQUATE(2013),
	// Message: You cannot proceed because an error has occurred.
	YOU_CANNOT_PROCEED_BECAUSE_AN_ERROR_HAS_OCCURRED(2014),
	// Message: A skill is ready to be used again.
	A_SKILL_IS_READY_TO_BE_USED_AGAIN(2015),
	// Message: A skill is ready to be used again but its re-use counter time has increased.
	A_SKILL_IS_READY_TO_BE_USED_AGAIN_BUT_ITS_REUSE_COUNTER_TIME_HAS_INCREASED(2016),
	// Message: $s1 cannot duel because $s1 is currently engaged in a private store or manufacture.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE(2017),
	// Message: $s1 cannot duel because $s1 is currently fishing.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING(2018),
	// Message: $s1 cannot duel because $s1's HP or MP is below 50 percent.
	S1_CANNOT_DUEL_BECAUSE_S1S_HP_OR_MP_IS_BELOW_50_PERCENT(2019),
	// Message: $s1 cannot make a challenge to a duel because $s1 is currently in a duel-prohibited area (Peaceful Zone / Seven Signs Zone / Near Water / Restart Prohibited Area).
	S1_CANNOT_MAKE_A_CHALLENGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUELPROHIBITED_AREA_PEACEFUL_ZONE__SEVEN_SIGNS_ZONE__NEAR_WATER__RESTART_PROHIBITED_AREA(2020),
	// Message: $s1 cannot duel because $s1 is currently engaged in battle.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE(2021),
	// Message: $s1 cannot duel because $s1 is already engaged in a duel.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL(2022),
	// Message: $s1 cannot duel because $s1 is in a chaotic state.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE(2023),
	// Message: $s1 cannot duel because $s1 is participating in the Olympiad.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD(2024),
	// Message: $s1 cannot duel because $s1 is participating in a clan hall war.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_CLAN_HALL_WAR(2025),
	// Message: $s1 cannot duel because $s1 is participating in a siege war.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_A_SIEGE_WAR(2026),
	// Message: $s1 cannot duel because $s1 is currently riding or boat, steed, or strider.
	S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_OR_BOAT_STEED_OR_STRIDER(2027),
	// Message: $s1 cannot receive a duel challenge because $s1 is too far away.
	S1_CANNOT_RECEIVE_A_DUEL_CHALLENGE_BECAUSE_S1_IS_TOO_FAR_AWAY(2028),
	// Message: You cannot participate in the Olympiad during teleport.
	YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD_DURING_TELEPORT(2029),
	// Message: You are currently logging in.
	YOU_ARE_CURRENTLY_LOGGING_IN(2030),
	// Message: Please wait a moment.
	PLEASE_WAIT_A_MOMENT(2031);

	private final L2GameServerPacket _message;
	private final int _id;
	private final byte _size;

	SystemMsg(int i)
	{
		_id = i;

		if(name().contains("S4") || name().contains("C4"))
		{
			_size = 4;
			_message = null;
		}
		else if(name().contains("S3") || name().contains("C3"))
		{
			_size = 3;
			_message = null;
		}
		else if(name().contains("S2") || name().contains("C2"))
		{
			_size = 2;
			_message = null;
		}
		else if(name().contains("S1") || name().contains("C1"))
		{
			_size = 1;
			_message = null;
		}
		else
		{
			_size = 0;
			_message = new SystemMessagePacket(this);
		}
	}

	public int getId()
	{
		return _id;
	}

	public byte size()
	{
		return _size;
	}

	public static SystemMsg valueOf(int id)
	{
		for(SystemMsg m : values())
			if(m.getId() == id)
				return m;

		throw new NoSuchElementException("Not find SystemMsg by id: " + id);
	}

	@Override
	public L2GameServerPacket packet(Player player)
	{
		if(_message == null)
			throw new NoSuchElementException("Running SystemMsg.packet(Player), but message require arguments: " + name());

		return _message;
	}
}
