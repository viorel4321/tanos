package l2s.gameserver.model.entity.SevenSignsFestival;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.instances.FestivalMonsterInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.Say2;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class L2DarknessFestival
{
	public static final int[][] FESTIVAL_DAWN_PLAYER_SPAWNS = new int[][] {
				{ -79187, 113186, -4895, 0 },
				{ -75918, 110137, -4895, 0 },
				{ -73835, 111969, -4895, 0 },
				{ -76170, 113804, -4895, 0 },
				{ -78927, 109528, -4895, 0 } };
	public static final int[][] FESTIVAL_DUSK_PLAYER_SPAWNS = new int[][] {
				{ -77200, 88966, -5151, 0 },
				{ -76941, 85307, -5151, 0 },
				{ -74855, 87135, -5151, 0 },
				{ -80208, 88222, -5151, 0 },
				{ -79954, 84697, -5151, 0 } };
	private static final int[][] FESTIVAL_DAWN_WITCH_SPAWNS = new int[][] {
				{ -79183, 113052, -4891, 0, 31132 },
				{ -75916, 110270, -4891, 0, 31133 },
				{ -73979, 111970, -4891, 0, 31134 },
				{ -76174, 113663, -4891, 0, 31135 },
				{ -78930, 109664, -4891, 0, 31136 } };
	private static final int[][] FESTIVAL_DUSK_WITCH_SPAWNS = new int[][] {
				{ -77199, 88830, -5147, 0, 31142 },
				{ -76942, 85438, -5147, 0, 31143 },
				{ -74990, 87135, -5147, 0, 31144 },
				{ -80207, 88222, -5147, 0, 31145 },
				{ -79952, 84833, -5147, 0, 31146 } };
	private static final int[][][] FESTIVAL_DAWN_PRIMARY_SPAWNS = new int[][][] {
				{
						{ -78537, 113839, -4895, -1, 18009 },
						{ -78466, 113852, -4895, -1, 18010 },
						{ -78509, 113899, -4895, -1, 18010 },
						{ -78481, 112557, -4895, -1, 18009 },
						{ -78559, 112504, -4895, -1, 18010 },
						{ -78489, 112494, -4895, -1, 18010 },
						{ -79803, 112543, -4895, -1, 18012 },
						{ -79854, 112492, -4895, -1, 18013 },
						{ -79886, 112557, -4895, -1, 18014 },
						{ -79821, 113811, -4895, -1, 18015 },
						{ -79857, 113896, -4895, -1, 18017 },
						{ -79878, 113816, -4895, -1, 18018 },
						{ -79190, 113660, -4895, -1, 18011 },
						{ -78710, 113188, -4895, -1, 18011 },
						{ -79190, 112730, -4895, -1, 18016 },
						{ -79656, 113188, -4895, -1, 18016 } },
				{
						{ -76558, 110784, -4895, -1, 18019 },
						{ -76607, 110815, -4895, -1, 18020 },
						{ -76559, 110820, -4895, -1, 18020 },
						{ -75277, 110792, -4895, -1, 18019 },
						{ -75225, 110801, -4895, -1, 18020 },
						{ -75262, 110832, -4895, -1, 18020 },
						{ -75249, 109441, -4895, -1, 18022 },
						{ -75278, 109495, -4895, -1, 18023 },
						{ -75223, 109489, -4895, -1, 18024 },
						{ -76556, 109490, -4895, -1, 18025 },
						{ -76607, 109469, -4895, -1, 18027 },
						{ -76561, 109450, -4895, -1, 18028 },
						{ -76399, 110144, -4895, -1, 18021 },
						{ -75912, 110606, -4895, -1, 18021 },
						{ -75444, 110144, -4895, -1, 18026 },
						{ -75930, 109665, -4895, -1, 18026 } },
				{
						{ -73184, 111319, -4895, -1, 18029 },
						{ -73135, 111294, -4895, -1, 18030 },
						{ -73185, 111281, -4895, -1, 18030 },
						{ -74477, 111321, -4895, -1, 18029 },
						{ -74523, 111293, -4895, -1, 18030 },
						{ -74481, 111280, -4895, -1, 18030 },
						{ -74489, 112604, -4895, -1, 18032 },
						{ -74491, 112660, -4895, -1, 18033 },
						{ -74527, 112629, -4895, -1, 18034 },
						{ -73197, 112621, -4895, -1, 18035 },
						{ -73142, 112631, -4895, -1, 18037 },
						{ -73182, 112656, -4895, -1, 18038 },
						{ -73834, 112430, -4895, -1, 18031 },
						{ -74299, 111959, -4895, -1, 18031 },
						{ -73841, 111491, -4895, -1, 18036 },
						{ -73363, 111959, -4895, -1, 18036 } },
				{
						{ -75543, 114461, -4895, -1, 18039 },
						{ -75514, 114493, -4895, -1, 18040 },
						{ -75488, 114456, -4895, -1, 18040 },
						{ -75521, 113158, -4895, -1, 18039 },
						{ -75504, 113110, -4895, -1, 18040 },
						{ -75489, 113142, -4895, -1, 18040 },
						{ -76809, 113143, -4895, -1, 18042 },
						{ -76860, 113138, -4895, -1, 18043 },
						{ -76831, 113112, -4895, -1, 18044 },
						{ -76831, 114441, -4895, -1, 18045 },
						{ -76840, 114490, -4895, -1, 18047 },
						{ -76864, 114455, -4895, -1, 18048 },
						{ -75703, 113797, -4895, -1, 18041 },
						{ -76180, 114263, -4895, -1, 18041 },
						{ -76639, 113797, -4895, -1, 18046 },
						{ -76180, 113337, -4895, -1, 18046 } },
				{
						{ -79576, 108881, -4895, -1, 18049 },
						{ -79592, 108835, -4895, -1, 18050 },
						{ -79614, 108871, -4895, -1, 18050 },
						{ -79586, 110171, -4895, -1, 18049 },
						{ -79589, 110216, -4895, -1, 18050 },
						{ -79620, 110177, -4895, -1, 18050 },
						{ -78825, 110182, -4895, -1, 18052 },
						{ -78238, 110182, -4895, -1, 18053 },
						{ -78266, 110218, -4895, -1, 18054 },
						{ -78275, 108883, -4895, -1, 18055 },
						{ -78267, 108839, -4895, -1, 18057 },
						{ -78241, 108871, -4895, -1, 18058 },
						{ -79394, 109538, -4895, -1, 18051 },
						{ -78929, 109992, -4895, -1, 18051 },
						{ -78454, 109538, -4895, -1, 18056 },
						{ -78929, 109053, -4895, -1, 18056 } } };
	private static final int[][][] FESTIVAL_DUSK_PRIMARY_SPAWNS = new int[][][] {
				{
						{ -76542, 89653, -5151, -1, 18009 },
						{ -76509, 89637, -5151, -1, 18010 },
						{ -76548, 89614, -5151, -1, 18010 },
						{ -76539, 88326, -5151, -1, 18009 },
						{ -76512, 88289, -5151, -1, 18010 },
						{ -76546, 88287, -5151, -1, 18010 },
						{ -77879, 88308, -5151, -1, 18012 },
						{ -77886, 88310, -5151, -1, 18013 },
						{ -77879, 88278, -5151, -1, 18014 },
						{ -77857, 89605, -5151, -1, 18015 },
						{ -77858, 89658, -5151, -1, 18017 },
						{ -77891, 89633, -5151, -1, 18018 },
						{ -76728, 88962, -5151, -1, 18011 },
						{ -77194, 88494, -5151, -1, 18011 },
						{ -77660, 88896, -5151, -1, 18016 },
						{ -77195, 89438, -5151, -1, 18016 } },
				{
						{ -77585, 84650, -5151, -1, 18019 },
						{ -77628, 84643, -5151, -1, 18020 },
						{ -77607, 84613, -5151, -1, 18020 },
						{ -76603, 85946, -5151, -1, 18019 },
						{ -77606, 85994, -5151, -1, 18020 },
						{ -77638, 85959, -5151, -1, 18020 },
						{ -76301, 85960, -5151, -1, 18022 },
						{ -76257, 85972, -5151, -1, 18023 },
						{ -76286, 85992, -5151, -1, 18024 },
						{ -76281, 84667, -5151, -1, 18025 },
						{ -76291, 84611, -5151, -1, 18027 },
						{ -76257, 84616, -5151, -1, 18028 },
						{ -77419, 85307, -5151, -1, 18021 },
						{ -76952, 85768, -5151, -1, 18021 },
						{ -76477, 85312, -5151, -1, 18026 },
						{ -76942, 84832, -5151, -1, 18026 } },
				{
						{ -74211, 86494, -5151, -1, 18029 },
						{ -74200, 86449, -5151, -1, 18030 },
						{ -74167, 86464, -5151, -1, 18030 },
						{ -75495, 86482, -5151, -1, 18029 },
						{ -75540, 86473, -5151, -1, 18030 },
						{ -75509, 86445, -5151, -1, 18030 },
						{ -75509, 87775, -5151, -1, 18032 },
						{ -75518, 87826, -5151, -1, 18033 },
						{ -75542, 87780, -5151, -1, 18034 },
						{ -74214, 87789, -5151, -1, 18035 },
						{ -74169, 87801, -5151, -1, 18037 },
						{ -74198, 87827, -5151, -1, 18038 },
						{ -75324, 87135, -5151, -1, 18031 },
						{ -74852, 87606, -5151, -1, 18031 },
						{ -74388, 87146, -5151, -1, 18036 },
						{ -74856, 86663, -5151, -1, 18036 } },
				{
						{ -79560, 89007, -5151, -1, 18039 },
						{ -79521, 89016, -5151, -1, 18040 },
						{ -79544, 89047, -5151, -1, 18040 },
						{ -79552, 87717, -5151, -1, 18039 },
						{ -79552, 87673, -5151, -1, 18040 },
						{ -79510, 87702, -5151, -1, 18040 },
						{ -80866, 87719, -5151, -1, 18042 },
						{ -80897, 87689, -5151, -1, 18043 },
						{ -80850, 87685, -5151, -1, 18044 },
						{ -80848, 89013, -5151, -1, 18045 },
						{ -80887, 89051, -5151, -1, 18047 },
						{ -80891, 89004, -5151, -1, 18048 },
						{ -80205, 87895, -5151, -1, 18041 },
						{ -80674, 88350, -5151, -1, 18041 },
						{ -80209, 88833, -5151, -1, 18046 },
						{ -79743, 88364, -5151, -1, 18046 } },
				{
						{ -80624, 84060, -5151, -1, 18049 },
						{ -80621, 84007, -5151, -1, 18050 },
						{ -80590, 84039, -5151, -1, 18050 },
						{ -80605, 85349, -5151, -1, 18049 },
						{ -80639, 85363, -5151, -1, 18050 },
						{ -80611, 85385, -5151, -1, 18050 },
						{ -79311, 85353, -5151, -1, 18052 },
						{ -79277, 85384, -5151, -1, 18053 },
						{ -79273, 85539, -5151, -1, 18054 },
						{ -79297, 84054, -5151, -1, 18055 },
						{ -79285, 84006, -5151, -1, 18057 },
						{ -79260, 84040, -5151, -1, 18058 },
						{ -79945, 85171, -5151, -1, 18051 },
						{ -79489, 84707, -5151, -1, 18051 },
						{ -79952, 84222, -5151, -1, 18056 },
						{ -80423, 84703, -5151, -1, 18056 } } };
	private static final int[][][] FESTIVAL_DAWN_SECONDARY_SPAWNS = new int[][][] {
				{
						{ -78757, 112834, -4895, -1, 18016 },
						{ -78581, 112834, -4895, -1, 18016 },
						{ -78822, 112526, -4895, -1, 18011 },
						{ -78822, 113702, -4895, -1, 18011 },
						{ -78822, 113874, -4895, -1, 18011 },
						{ -79524, 113546, -4895, -1, 18011 },
						{ -79693, 113546, -4895, -1, 18011 },
						{ -79858, 113546, -4895, -1, 18011 },
						{ -79545, 112757, -4895, -1, 18016 },
						{ -79545, 112586, -4895, -1, 18016 } },
				{
						{ -75565, 110580, -4895, -1, 18026 },
						{ -75565, 110740, -4895, -1, 18026 },
						{ -75577, 109776, -4895, -1, 18021 },
						{ -75413, 109776, -4895, -1, 18021 },
						{ -75237, 109776, -4895, -1, 18021 },
						{ -76274, 109468, -4895, -1, 18021 },
						{ -76274, 109635, -4895, -1, 18021 },
						{ -76274, 109795, -4895, -1, 18021 },
						{ -76351, 110500, -4895, -1, 18056 },
						{ -76528, 110500, -4895, -1, 18056 } },
				{
						{ -74191, 111527, -4895, -1, 18036 },
						{ -74191, 111362, -4895, -1, 18036 },
						{ -73495, 111611, -4895, -1, 18031 },
						{ -73327, 111611, -4895, -1, 18031 },
						{ -73154, 111611, -4895, -1, 18031 },
						{ -73473, 112301, -4895, -1, 18031 },
						{ -73473, 112475, -4895, -1, 18031 },
						{ -73473, 118036, -4895, -1, 18031 },
						{ -74270, 112326, -4895, -1, 18036 },
						{ -74443, 112326, -4895, -1, 18036 } },
				{
						{ -75738, 113439, -4895, -1, 18046 },
						{ -75571, 113439, -4895, -1, 18046 },
						{ -75824, 114141, -4895, -1, 18041 },
						{ -75824, 114309, -4895, -1, 18041 },
						{ -75824, 114477, -4895, -1, 18041 },
						{ -76513, 114158, -4895, -1, 18041 },
						{ -76683, 114158, -4895, -1, 18041 },
						{ -76857, 114158, -4895, -1, 18041 },
						{ -76535, 113357, -4895, -1, 18056 },
						{ -76535, 113190, -4895, -1, 18056 } },
				{
						{ -79350, 109894, -4895, -1, 18056 },
						{ -79534, 109894, -4895, -1, 18056 },
						{ -79285, 109187, -4895, -1, 18051 },
						{ -79285, 109019, -4895, -1, 18051 },
						{ -79285, 108860, -4895, -1, 18051 },
						{ -78587, 109172, -4895, -1, 18051 },
						{ -78415, 109172, -4895, -1, 18051 },
						{ -78249, 109172, -4895, -1, 18051 },
						{ -78575, 109961, -4895, -1, 18056 },
						{ -78575, 110130, -4895, -1, 18056 } } };
	private static final int[][][] FESTIVAL_DUSK_SECONDARY_SPAWNS = new int[][][] {
				{
						{ -76844, 89304, -5151, -1, 18011 },
						{ -76844, 89479, -5151, -1, 18011 },
						{ -76844, 89649, -5151, -1, 18011 },
						{ -77544, 89326, -5151, -1, 18011 },
						{ -77716, 89326, -5151, -1, 18011 },
						{ -77881, 89326, -5151, -1, 18011 },
						{ -77561, 88530, -5151, -1, 18016 },
						{ -77561, 88364, -5151, -1, 18016 },
						{ -76762, 88615, -5151, -1, 18016 },
						{ -76594, 88615, -5151, -1, 18016 } },
				{
						{ -77307, 84969, -5151, -1, 18021 },
						{ -77307, 84795, -5151, -1, 18021 },
						{ -77307, 84623, -5151, -1, 18021 },
						{ -76614, 84944, -5151, -1, 18021 },
						{ -76433, 84944, -5151, -1, 18021 },
						{ -76261, 84944, -5151, -1, 18021 },
						{ -76594, 85745, -5151, -1, 18026 },
						{ -76594, 85910, -5151, -1, 18026 },
						{ -77384, 85660, -5151, -1, 18026 },
						{ -77555, 85660, -5151, -1, 18026 } },
				{
						{ -74517, 86782, -5151, -1, 18031 },
						{ -74344, 86782, -5151, -1, 18031 },
						{ -74185, 86782, -5151, -1, 18031 },
						{ -74496, 87464, -5151, -1, 18031 },
						{ -74496, 87636, -5151, -1, 18031 },
						{ -74496, 87815, -5151, -1, 18031 },
						{ -75298, 87497, -5151, -1, 18036 },
						{ -75460, 87497, -5151, -1, 18036 },
						{ -75219, 86712, -5151, -1, 18036 },
						{ -75219, 86531, -5151, -1, 18036 } },
				{
						{ -79851, 88703, -5151, -1, 18041 },
						{ -79851, 88868, -5151, -1, 18041 },
						{ -79851, 89040, -5151, -1, 18041 },
						{ -80548, 88722, -5151, -1, 18041 },
						{ -80711, 88722, -5151, -1, 18041 },
						{ -80883, 88722, -5151, -1, 18041 },
						{ -80565, 87916, -5151, -1, 18046 },
						{ -80565, 87752, -5151, -1, 18046 },
						{ -79779, 87996, -5151, -1, 18046 },
						{ -79613, 87996, -5151, -1, 18046 } },
				{
						{ -79271, 84330, -5151, -1, 18051 },
						{ -79448, 84330, -5151, -1, 18051 },
						{ -79601, 84330, -5151, -1, 18051 },
						{ -80311, 84367, -5151, -1, 18051 },
						{ -80311, 84196, -5151, -1, 18051 },
						{ -80311, 84015, -5151, -1, 18051 },
						{ -80556, 85049, -5151, -1, 18056 },
						{ -80384, 85049, -5151, -1, 18056 },
						{ -79598, 85127, -5151, -1, 18056 },
						{ -79598, 85303, -5151, -1, 18056 } } };
	private static final int[][][] FESTIVAL_DAWN_CHEST_SPAWNS = new int[][][] {
				{
						{ -78999, 112957, -4927, -1, 18109 },
						{ -79153, 112873, -4927, -1, 18109 },
						{ -79256, 112873, -4927, -1, 18109 },
						{ -79368, 112957, -4927, -1, 18109 },
						{ -79481, 113124, -4927, -1, 18109 },
						{ -79481, 113275, -4927, -1, 18109 },
						{ -79364, 113398, -4927, -1, 18109 },
						{ -79213, 113500, -4927, -1, 18109 },
						{ -79099, 113500, -4927, -1, 18109 },
						{ -78960, 113398, -4927, -1, 18109 },
						{ -78882, 113235, -4927, -1, 18109 },
						{ -78882, 113099, -4927, -1, 18109 } },
				{
						{ -76119, 110383, -4927, -1, 18110 },
						{ -75980, 110442, -4927, -1, 18110 },
						{ -75848, 110442, -4927, -1, 18110 },
						{ -75720, 110383, -4927, -1, 18110 },
						{ -75625, 110195, -4927, -1, 18110 },
						{ -75625, 110063, -4927, -1, 18110 },
						{ -75722, 109908, -4927, -1, 18110 },
						{ -75863, 109832, -4927, -1, 18110 },
						{ -75989, 109832, -4927, -1, 18110 },
						{ -76130, 109908, -4927, -1, 18110 },
						{ -76230, 110079, -4927, -1, 18110 },
						{ -76230, 110215, -4927, -1, 18110 } },
				{
						{ -74055, 111781, -4927, -1, 18111 },
						{ -74144, 111938, -4927, -1, 18111 },
						{ -74144, 112075, -4927, -1, 18111 },
						{ -74055, 112173, -4927, -1, 18111 },
						{ -73885, 112289, -4927, -1, 18111 },
						{ -73756, 112289, -4927, -1, 18111 },
						{ -73574, 112141, -4927, -1, 18111 },
						{ -73511, 112040, -4927, -1, 18111 },
						{ -73511, 111912, -4927, -1, 18111 },
						{ -73574, 111772, -4927, -1, 18111 },
						{ -73767, 111669, -4927, -1, 18111 },
						{ -73899, 111669, -4927, -1, 18111 } },
				{
						{ -76008, 113566, -4927, -1, 18112 },
						{ -76159, 113485, -4927, -1, 18112 },
						{ -76267, 113485, -4927, -1, 18112 },
						{ -76386, 113566, -4927, -1, 18112 },
						{ -76482, 113748, -4927, -1, 18112 },
						{ -76482, 113885, -4927, -1, 18112 },
						{ -76371, 114029, -4927, -1, 18112 },
						{ -76220, 114118, -4927, -1, 18112 },
						{ -76092, 114118, -4927, -1, 18112 },
						{ -75975, 114029, -4927, -1, 18112 },
						{ -75861, 113851, -4927, -1, 18112 },
						{ -75861, 113713, -4927, -1, 18112 } },
				{
						{ -79100, 109782, -4927, -1, 18113 },
						{ -78962, 109853, -4927, -1, 18113 },
						{ -78851, 109853, -4927, -1, 18113 },
						{ -78721, 109782, -4927, -1, 18113 },
						{ -78615, 109596, -4927, -1, 18113 },
						{ -78615, 109453, -4927, -1, 18113 },
						{ -78746, 109300, -4927, -1, 18113 },
						{ -78881, 109203, -4927, -1, 18113 },
						{ -79027, 109203, -4927, -1, 18113 },
						{ -79159, 109300, -4927, -1, 18113 },
						{ -79240, 109480, -4927, -1, 18113 },
						{ -79240, 109615, -4927, -1, 18113 } } };
	private static final int[][][] FESTIVAL_DUSK_CHEST_SPAWNS = new int[][][] {
				{
						{ -77016, 88726, -5183, -1, 18114 },
						{ -77136, 88646, -5183, -1, 18114 },
						{ -77247, 88646, -5183, -1, 18114 },
						{ -77380, 88726, -5183, -1, 18114 },
						{ -77512, 88883, -5183, -1, 18114 },
						{ -77512, 89053, -5183, -1, 18114 },
						{ -77378, 89287, -5183, -1, 18114 },
						{ -77254, 89238, -5183, -1, 18114 },
						{ -77095, 89238, -5183, -1, 18114 },
						{ -76996, 89287, -5183, -1, 18114 },
						{ -76901, 89025, -5183, -1, 18114 },
						{ -76901, 88891, -5183, -1, 18114 } },
				{
						{ -77128, 85553, -5183, -1, 18115 },
						{ -77036, 85594, -5183, -1, 18115 },
						{ -76919, 85594, -5183, -1, 18115 },
						{ -76755, 85553, -5183, -1, 18115 },
						{ -76635, 85392, -5183, -1, 18115 },
						{ -76635, 85216, -5183, -1, 18115 },
						{ -76761, 85025, -5183, -1, 18115 },
						{ -76908, 85004, -5183, -1, 18115 },
						{ -77041, 85004, -5183, -1, 18115 },
						{ -77138, 85025, -5183, -1, 18115 },
						{ -77268, 85219, -5183, -1, 18115 },
						{ -77268, 85410, -5183, -1, 18115 } },
				{
						{ -75150, 87303, -5183, -1, 18116 },
						{ -75150, 87175, -5183, -1, 18116 },
						{ -75150, 87175, -5183, -1, 18116 },
						{ -75150, 87303, -5183, -1, 18116 },
						{ -74943, 87433, -5183, -1, 18116 },
						{ -74767, 87433, -5183, -1, 18116 },
						{ -74556, 87306, -5183, -1, 18116 },
						{ -74556, 87184, -5183, -1, 18116 },
						{ -74556, 87184, -5183, -1, 18116 },
						{ -74556, 87306, -5183, -1, 18116 },
						{ -74757, 86830, -5183, -1, 18116 },
						{ -74927, 86830, -5183, -1, 18116 } },
				{
						{ -80010, 88128, -5183, -1, 18117 },
						{ -80113, 88066, -5183, -1, 18117 },
						{ -80220, 88066, -5183, -1, 18117 },
						{ -80359, 88128, -5183, -1, 18117 },
						{ -80467, 88267, -5183, -1, 18117 },
						{ -80467, 88436, -5183, -1, 18117 },
						{ -80381, 88639, -5183, -1, 18117 },
						{ -80278, 88577, -5183, -1, 18117 },
						{ -80142, 88577, -5183, -1, 18117 },
						{ -80028, 88639, -5183, -1, 18117 },
						{ -79915, 88466, -5183, -1, 18117 },
						{ -79915, 88322, -5183, -1, 18117 } },
				{
						{ -80153, 84947, -5183, -1, 18118 },
						{ -80003, 84962, -5183, -1, 18118 },
						{ -79848, 84962, -5183, -1, 18118 },
						{ -79742, 84947, -5183, -1, 18118 },
						{ -79668, 84772, -5183, -1, 18118 },
						{ -79668, 84619, -5183, -1, 18118 },
						{ -79772, 84471, -5183, -1, 18118 },
						{ -79888, 84414, -5183, -1, 18118 },
						{ -80023, 84414, -5183, -1, 18118 },
						{ -80166, 84471, -5183, -1, 18118 },
						{ -80253, 84600, -5183, -1, 18118 },
						{ -80253, 84780, -5183, -1, 18118 } } };

	private static Logger _log = LoggerFactory.getLogger(L2DarknessFestival.class);

	private int _cabal;
	private int _levelRange;
	private boolean _challengeIncreased;
	private FestivalSpawn _startLocation;
	private FestivalSpawn _witchSpawn;
	private NpcInstance _witchInst;
	private List<FestivalMonsterInstance> _npcInsts;
	private List<Player> _participants;
	private Map<Player, FestivalSpawn> _originalLocations;

	public L2DarknessFestival(final int cabal, final int levelRange)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_cabal = cabal;
		_levelRange = levelRange;
		_originalLocations = new HashMap<Player, FestivalSpawn>();
		_npcInsts = new CopyOnWriteArrayList<FestivalMonsterInstance>();
		if(cabal == 2)
		{
			_participants = SevenSignsFestival.getDawnFestivalParticipants().get(levelRange);
			_witchSpawn = new FestivalSpawn(FESTIVAL_DAWN_WITCH_SPAWNS[levelRange]);
			_startLocation = new FestivalSpawn(FESTIVAL_DAWN_PLAYER_SPAWNS[levelRange]);
		}
		else
		{
			_participants = SevenSignsFestival.getDuskFestivalParticipants().get(levelRange);
			_witchSpawn = new FestivalSpawn(FESTIVAL_DUSK_WITCH_SPAWNS[levelRange]);
			_startLocation = new FestivalSpawn(FESTIVAL_DUSK_PLAYER_SPAWNS[levelRange]);
		}
		if(_participants == null)
			_participants = new CopyOnWriteArrayList<Player>();
		festivalInit();
	}

	private void festivalInit()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_log.info("SevenSignsFestival: Initializing festival for " + SevenSigns.getCabalShortName(_cabal) + " (" + SevenSignsFestival.getFestivalName(_levelRange) + ")");
		if(_participants.size() > 0)
		{
			for(final Player participant : _participants)
			{
				_originalLocations.put(participant, new FestivalSpawn(participant.getLoc()));
				participant.teleToLocation(Location.findPointToStay(_startLocation.loc, 30, 230, participant.getGeoIndex()));
				participant.getAbnormalList().stopAll();
				final ItemInstance offer = participant.getInventory().findItemByItemId(5901);
				if(offer != null)
					participant.getInventory().destroyItemByItemId(5901, offer.getCount(), true);
			}
		}

		final NpcTemplate witchTemplate = NpcTable.getTemplate(_witchSpawn.npcId);
		try
		{
			final Spawn npcSpawn = new Spawn(witchTemplate);
			npcSpawn.setLoc(_witchSpawn.loc);
			_witchInst = npcSpawn.doSpawn(true);
		}
		catch(Exception e)
		{
			_log.warn("SevenSignsFestival: Error while spawning Festival Witch ID " + _witchSpawn.npcId + ": " + e);
		}

		_witchInst.broadcastPacket(new MagicSkillUse(_witchInst, _witchInst, 2003, 1, 1, 0L));
		_witchInst.broadcastPacket(new MagicSkillUse(_witchInst, _witchInst, 2133, 1, 1, 0L));

		sendMessageToParticipants("The festival will begin in 2 minutes.");
	}

	public void festivalStart()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_log.info("SevenSignsFestival: Starting festival for " + SevenSigns.getCabalShortName(_cabal) + " (" + SevenSignsFestival.getFestivalName(_levelRange) + ")");
		spawnFestivalMonsters(60, 0);
	}

	public void moveMonstersToCenter()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		for(final FestivalMonsterInstance festivalMob : _npcInsts)
		{
			if(festivalMob.isDead())
				continue;
			final CtrlIntention currIntention = festivalMob.getAI().getIntention();
			if(currIntention != CtrlIntention.AI_INTENTION_IDLE && currIntention != CtrlIntention.AI_INTENTION_ACTIVE)
				continue;
			festivalMob.setRunning();
			festivalMob.moveToLocation(Location.findPointToStay(_startLocation.loc, 30, 230, festivalMob.getGeoIndex()), 0, true);
		}
	}

	public void spawnFestivalMonsters(final int respawnDelay, final int spawnType)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		int[][] _npcSpawns = null;
		switch(spawnType)
		{
			case 0:
			case 1:
			{
				_npcSpawns = _cabal == 2 ? FESTIVAL_DAWN_PRIMARY_SPAWNS[_levelRange] : FESTIVAL_DUSK_PRIMARY_SPAWNS[_levelRange];
				break;
			}
			case 2:
			{
				_npcSpawns = _cabal == 2 ? FESTIVAL_DAWN_SECONDARY_SPAWNS[_levelRange] : FESTIVAL_DUSK_SECONDARY_SPAWNS[_levelRange];
				break;
			}
			case 3:
			{
				_npcSpawns = _cabal == 2 ? FESTIVAL_DAWN_CHEST_SPAWNS[_levelRange] : FESTIVAL_DUSK_CHEST_SPAWNS[_levelRange];
				break;
			}
			default:
			{
				return;
			}
		}
		for(final int[] element : _npcSpawns)
		{
			final FestivalSpawn currSpawn = new FestivalSpawn(element);
			final NpcTemplate npcTemplate = NpcTable.getTemplate(currSpawn.npcId);
			try
			{
				final Spawn npcSpawn = new Spawn(npcTemplate);
				npcSpawn.setLoc(currSpawn.loc);
				npcSpawn.setHeading(Rnd.get(65536));
				npcSpawn.setAmount(1);
				npcSpawn.setRespawnDelay(respawnDelay);
				npcSpawn.startRespawn();
				final FestivalMonsterInstance festivalMob = (FestivalMonsterInstance) npcSpawn.doSpawn(true);
				if(spawnType == 1)
					festivalMob.setOfferingBonus(2);
				else if(spawnType == 3)
					festivalMob.setOfferingBonus(5);
				_npcInsts.add(festivalMob);
			}
			catch(Exception e)
			{
				_log.warn("SevenSignsFestival: Error while spawning NPC ID " + currSpawn.npcId + ": " + e);
			}
		}
	}

	public boolean increaseChallenge()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return false;
		if(_challengeIncreased)
			return false;
		_challengeIncreased = true;
		spawnFestivalMonsters(60, 1);
		return true;
	}

	public void sendMessageToParticipants(final String message)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		if(_participants.size() > 0)
		{
			for(final Player participant : _participants)
				participant.sendPacket(new Say2(_witchInst.getObjectId(), ChatType.ALL, "Festival Witch", message));
		}
	}

	public void festivalEnd()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_log.info("SevenSignsFestival: Ending festival for " + SevenSigns.getCabalShortName(_cabal) + " (" + SevenSignsFestival.getFestivalName(_levelRange) + ")");
		if(_participants.size() > 0)
		{
			for(final Player participant : _participants)
			{
				relocatePlayer(participant, false);
				participant.sendMessage(new CustomMessage("l2s.gameserver.model.entity.SevenSignsFestival.Ended"));
			}
			if(_cabal == 2)
				SevenSignsFestival.getDawnPreviousParticipants().put(_levelRange, _participants);
			else
				SevenSignsFestival.getDuskPreviousParticipants().put(_levelRange, _participants);
		}
		if(_witchInst != null)
			_witchInst.deleteMe();
		for(final FestivalMonsterInstance monsterInst : _npcInsts)
			if(monsterInst != null)
				monsterInst.deleteMe();
	}

	public void relocatePlayer(final Player participant, final boolean isRemoving)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		try
		{
			final FestivalSpawn origPosition = _originalLocations.get(participant);
			if(isRemoving)
				_originalLocations.remove(participant);
			participant.teleToLocation(origPosition.loc);
		}
		catch(Exception e)
		{
			participant.teleToClosestTown();
		}
		participant.sendMessage(new CustomMessage("l2s.gameserver.model.entity.SevenSignsFestival.Removed"));
	}
}
