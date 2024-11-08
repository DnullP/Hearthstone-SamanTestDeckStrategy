import club.xiaojiawei.bean.Card
import club.xiaojiawei.bean.Player
import club.xiaojiawei.bean.area.PlayArea
import club.xiaojiawei.config.log
import club.xiaojiawei.enums.CardRaceEnum
import club.xiaojiawei.enums.CardTypeEnum
import com.sun.org.apache.xpath.internal.operations.Bool

object DeckUtil {
     fun haveCard(cardID: String, cards: List<Card>): Card? {
        for (card in cards) {
            if (card.cardId == cardID) {
                return card
            }
        }
        return null
    }
    fun costEnough(card: Card, me: Player): Boolean {
        if (card.cost <= me.usableResource) {
            log.info { "法力值需要: ${card.cost}/${me.usableResource} (可以打出 ${card.cardId})" }
            return true
        }else {
            return false
        }
    }
    fun getCoin(cards: List<Card>,): List<Card> {
        var coinList = mutableListOf<Card>()
        for (card in cards) {
            if (card.isCoinCard) {
                coinList.add(card)
            }
        }
        log.info { "手上可用硬币数量: ${coinList.size}" }
        return coinList
    }
    fun tryCoins(card: Card, me: Player, coins: List<Card>): Boolean {
        if (card.cost <= coins.size + me.usableResource) {
            for(coin in coins) {
                if (me.usableResource < card.cost) {
                    coin.action.power()
                }else{
                    break
                }
            }
            log.info { "尝试硬币跳费成功" }
            return true
        }
        return false
    }
    fun getRivalTaunt(rival: Player): List<Card> {
        var tauntList = mutableListOf<Card>()
        for (card in rival.playArea.cards){
            if (card.isTaunt) {
                tauntList.add(card)
            }
        }
        log.info { "场上存在地方嘲讽随从: ${tauntList.size}" }
        return tauntList
    }
    fun tryAttackFace(card: Card, rival: Player): Boolean {
        if (card.area is PlayArea && card.canAttack() && !card.isAttackableByRush) {
            var tauntList = getRivalTaunt(rival)
            if (tauntList.isNotEmpty()) {
                log.info { "不能打脸, 存在嘲讽, 优先攻击嘲讽" }
                card.action.attack(tauntList[0])
                return true
            }else {
                log.info { "打脸!!!" }
                card.action.attackHero()
                return true
            }
        }
        log.info { "[Warning]在战场: ${card.area is PlayArea} 可攻击: ${card.canAttack()} 可突袭: ${card.isAttackableByRush}" }
        return false
    }
    fun getBestFitEnemyPlay(health: Int, rival: Player): Card? {
        var bestFit: Card? = null
        var bestHealth = 0
        for (card in rival.playArea.cards) {
            if (card.health <= health && card.health >bestHealth) {
                bestHealth = card.health
                bestFit = card
            }
        }
        if (bestFit!=null){
            log.info { "得到最佳攻击目标: ${bestFit.cardId}" }
        }
        else {
            log.info {"没有最佳攻击目标"}
        }
        return bestFit
    }
    fun tryCleanEnemyPlay(playcard: Card, rival: Player ): Boolean {

        log.info { "可以攻击: ${playcard.canAttack()} 可以突袭:" +
                " ${playcard.isAttackableByRush} 是个突袭: ${playcard.isRush}"  }

        if (playcard.area is PlayArea && (playcard.canAttack() || playcard.isAttackableByRush)
            && rival.playArea.cards.isNotEmpty()) {

            var target = getBestFitEnemyPlay(playcard.atc, rival)

            if (target != null) {
                playcard.action.attack(target)
                return true
            } else{
                log.info { "选择第一个地方随从攻击" }
                playcard.action.attack(rival.playArea.cards[0])
                return true
            }
        }
        return false
    }
    fun getMyMinion(me: Player): List<Card> {
        var myMinion = mutableListOf<Card>()
        for (card in me.playArea.cards){
            if (card.cardType == CardTypeEnum.MINION) {
                myMinion.add(card)
            }
        }
        return myMinion
    }
}