import club.xiaojiawei.DeckPlugin

/**
 * @author 肖嘉威
 * @date 2024/9/29 16:56
 */
class TemplatePlugin: DeckPlugin {
    override fun description(): String {
//        插件的描述
        return "适用于大哥萨的套牌策略"
    }

    override fun author(): String {
//        插件的作者
        return "Dnull_P"
    }

    override fun version(): String {
//        插件的版本号
        return "1.0.0-dev"
    }

    override fun id(): String {
        return "big_bro_scheme"
    }

    override fun name(): String {
//        插件的名字
        return "大哥萨策略"
    }

    override fun homeUrl(): String {
        return "https://github.com/xjw580/Deck-Plugin-Market/tree/master/Deck-Plugin-Template"
    }
}