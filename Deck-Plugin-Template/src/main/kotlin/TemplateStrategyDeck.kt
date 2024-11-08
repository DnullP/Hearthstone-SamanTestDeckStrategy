import club.xiaojiawei.DeckStrategy
import club.xiaojiawei.bean.Card
import club.xiaojiawei.bean.Player
import club.xiaojiawei.bean.isValid
import club.xiaojiawei.config.log
import club.xiaojiawei.enums.CardTypeEnum
import club.xiaojiawei.enums.RunModeEnum
import club.xiaojiawei.status.War
import java.util.HashSet
import kotlin.contracts.contract

/**
 * @author Dnull_P
 * @date 2024/11/8 16:27
 */
class TemplateStrategyDeck : DeckStrategy() {

    private var withChanger = false
    private var withHammer = false
    private var beastInDeck = true
    private var battleCryInDeck = true
    private var coinCount: Int? = 0
    private val bigbro: List<String> = listOf()

    override fun name(): String {
//        套牌策略名
        return "大哥萨"
    }

    override fun getRunMode(): Array<RunModeEnum> {
//        策略允许运行的模式
        return arrayOf(RunModeEnum.WILD, RunModeEnum.PRACTICE)
    }

    override fun deckCode(): String {
        return "AAEBAdOLBQiTCdy4A+m2BLzOBL/OBPajBsekBuupBgvvAZQDkQ7OD/a9Ao+UA9b1A7y2BPTyBeqYBqSnBgABA/SzBsekBvezBsekBujeBsekBgAA"
    }

    override fun id(): String {
        return "bigbrosha"
    }

    override fun executeChangeCard(cards: HashSet<Card>) {
        val toList = cards.toList()
        var withChanger = false
        var withCalling = false
        for (card in toList) {
            if (card.cardId == "DAL_052") {
                log.info { "拥有变形怪" }
                withChanger = true
                continue
            }
            //先祖召唤 我找到了
            if (card.cardId == "GVG_029" || card.cardId == "BOT_099") {
                log.info{"拥有随从召唤"}
                withCalling = true
                continue
            }
        }
        for (card in toList) {
            if (card.cardId == "DMF_705" && withChanger) {
                log.info { "拥有变形怪, 保留敲狼锤" }
                continue
            }
            if (card.cardId == "WW_440" && withCalling) {
                log.info { "拥有随从召唤, 保留雷马" }
                continue
            }
            if (card.cardId == "GVG_029" || card.cardId == "BOT_099" || card.isBattlecry) {
                log.info { "保留召唤卡牌" }
                continue
            }
            log.info { "移除卡牌: ${card.cardId}" }
            cards.remove(card)
        }
    }

    /**
     * 深度复制卡牌集合
     */
    fun deepCloneCards(sourceCards: List<Card>): MutableList<Card> {
        val copyCards = mutableListOf<Card>()
        sourceCards.forEach {
            copyCards.add(it.clone())
        }
        return copyCards
    }

    /**
     * 我的回合开始时将会自动调用此方法
     */
    override fun executeOutCard() {
//        需要投降时将needSurrender设为true
//        needSurrender = true
        //        我方玩家
        val me = War.me
        if (!me.isValid()) return
//        敌方玩家
        val rival = War.rival
        if (!rival.isValid()) return
//            获取战场信息

//            获取我方所有手牌
        val handCards = me.handArea.cards
//            获取我方所有场上的卡牌
        val playCards = me.playArea.cards
//            获取我方英雄
        val hero = me.playArea.hero
//            获取我方武器
        val weapon = me.playArea.weapon
//            获取我方技能
        val power = me.playArea.power
//            获取我方所有牌库中的卡牌
        val deckCards = me.deckArea.cards
//            我方当前可用水晶数
        val usableResource = me.usableResource

//            cardId是游戏写死的，每张牌的cardId都是唯一不变的，如同身份证号码，
        val heroCardId = hero?.cardId
//            entityId在每局游戏中是唯一的
        val heroEntityId = hero?.entityId

        var cardPrivilege = mutableListOf<CardDoing>()

        //先祖召唤
        cardPrivilege.add(CardDoing("GVG_029", handCards,
            fun(card:Card): Boolean {
                if (DeckUtil.haveCard("DAL_052", handCards) != null) {
                    log.info { "避免拉出沼泽变形怪, 取消打出先祖" }
                    return false
                }
                if (DeckUtil.costEnough(card, me)) {
                    card.action.power()
                }else{
                    if (DeckUtil.tryCoins(card,me,DeckUtil.getCoin(handCards))) {
                        card.action.power()
                    }
                }
                return true;
            }))
        //我找到了
        cardPrivilege.add(CardDoing("BOT_099", handCards,
            fun(card:Card): Boolean {
                if (DeckUtil.haveCard("DAL_052", handCards) != null) {
                    log.info { "避免拉出沼泽变形怪, 取消打出'我找到了'" }
                    return false
                }
                if (DeckUtil.costEnough(card, me)) {
                    card.action.power()
                }else{
                    if (DeckUtil.tryCoins(card,me,DeckUtil.getCoin(handCards))) {
                        card.action.power()
                    }
                }
                return true;
            }))
        //沼泽怪
        cardPrivilege.add(CardDoing("DAL_052", handCards,
            fun(card:Card): Boolean {
                if (DeckUtil.costEnough(card, me)) {
                    card.action.power()
                }else{
                    if (DeckUtil.tryCoins(card,me,DeckUtil.getCoin(handCards))) {
                        card.action.power()
                    }
                }
                return true;
            }))
        //先祖之魂 CS2_038
        cardPrivilege.add(CardDoing("CS2_038", handCards,
            fun(card:Card): Boolean {
                var myMinion = DeckUtil.getMyMinion(me)
                if (DeckUtil.costEnough(card, me) && myMinion.isNotEmpty()) {
                    card.action.power(myMinion[0])
                    return true
                }
                return false;
            }))
        //童话林地 TOY_507
        cardPrivilege.add(CardDoing("TOY_507", handCards,
            fun(card:Card): Boolean {
                if (!DeckUtil.costEnough(card, me)) {
                    return false
                }
                card.action.power()
                return true
            }
        ))
        //间歇热泉
        cardPrivilege.add(CardDoing("TSC_637", handCards,
            fun(card:Card): Boolean {
                if (!DeckUtil.costEnough(card, me)) {
                    return false
                }
                if (rival.playArea.cards.isNotEmpty()) {
                    log.info { "场上有随从, 热泉打随从" }
                    card.action.power(rival.playArea.cards[0])
                    return true
                }else{
                    log.info { "场上无随从, 热泉打英雄" }
                    card.action.power()?.pointTo(rival.playArea.hero!!)
                    return true
                }
                //unreachable now
                return false
            }
        ))
        // 敲狼锤 DMF_705
        cardPrivilege.add(CardDoing("DMF_705", handCards,
            fun(card:Card): Boolean {
                if (!DeckUtil.costEnough(card, me)) {
                    return false
                }
                card.action.power()
                //unreachable now
                return true
            }
        ))
        //衰变
        cardPrivilege.add(CardDoing("CFM_696", handCards,
            fun(card:Card): Boolean {
                if (!DeckUtil.costEnough(card, me)) {
                    return false
                }
                if (rival.playArea.cards.size >= 2) {
                    card.action.power()
                }else{
                    log.info { "地方场上: $rival.playArea.cards.size, 无需退化" }
                    return false
                }
                //unreachable now
                return false
            }
        ))




        //cardProvilige.add("DMF_705") //敲狼锤


        for (cardDoing in cardPrivilege) {
            cardDoing.runDecision()
        }

        var copyPlayCards = playCards.toMutableList()
//            攻击

        log.info { "场上随从总数: ${copyPlayCards.size}" }
        for (playCard in copyPlayCards) {
            log.info { "正在遍历场上随从: ${playCard.cardId}" }
            if (playCard.cardType == CardTypeEnum.LOCATION) {
                log.info { "使用地标: ${playCard.cardId}" }
                playCard.action.attackHero()
            }
            if (!DeckUtil.tryAttackFace(playCard, rival)){
                if (!DeckUtil.tryCleanEnemyPlay(playCard, rival)){
                    log.info { "随从什么也打不到: ${playCard.cardId}" }
                }
            }
        }

        if (me.playArea.weapon != null) {
            hero!!.action.attackHero()
        }
        me.playArea.power?.let {
            if (me.usableResource >= it.cost || it.cost == 0) {
                log.info { "可以使用技能" }
                it.action.lClick()
            }
        }
    }

    override fun executeDiscoverChooseCard(vararg cards: Card): Int {
        for ((index, card) in cards.withIndex()) {
            if (card.cardId == "GVG_029" || card.cardId == "BOT_099" || card.cardId == "TSC_637") {
                return index
            }
        }
        return 1
    }

}