import club.xiaojiawei.bean.Card
import club.xiaojiawei.bean.Player

class CardDoing(var cardID: String, var handCard: List<Card>,var action: (card: Card)->Boolean) {
    fun runDecision():Boolean {
        var card = DeckUtil.haveCard(cardID, handCard)
        if (card == null) {
            return false
        }
        return action(card)
    }
}