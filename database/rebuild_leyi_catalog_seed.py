#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path


SEED_PATH = Path(__file__).resolve().with_name("leyi_catalog_seed.json")


CATEGORIES = [
    {
        "id": 1,
        "key": "casual_snacks",
        "name": "休闲零食",
        "sort": 1,
        "children": [
            {"id": 101, "key": "chips_puffed", "name": "薯片膨化", "sort": 1},
            {"id": 102, "key": "rice_crackers", "name": "锅巴米饼", "sort": 2},
            {"id": 103, "key": "corn_popcorn", "name": "玉米卷爆米花", "sort": 3},
        ],
    },
    {
        "id": 2,
        "key": "biscuits_bakery",
        "name": "饼干烘焙",
        "sort": 2,
        "children": [
            {"id": 104, "key": "sandwich_biscuits", "name": "夹心饼干", "sort": 1},
            {"id": 105, "key": "cookies_wafers", "name": "曲奇威化", "sort": 2},
            {"id": 106, "key": "cakes_bread", "name": "面包蛋糕", "sort": 3},
        ],
    },
    {
        "id": 3,
        "key": "nuts_dried_fruits",
        "name": "坚果果干",
        "sort": 3,
        "children": [
            {"id": 107, "key": "daily_nuts", "name": "每日坚果", "sort": 1},
            {"id": 108, "key": "plain_nuts", "name": "原味坚果", "sort": 2},
            {"id": 109, "key": "dried_fruits", "name": "果干蜜饯", "sort": 3},
        ],
    },
    {
        "id": 4,
        "key": "sweets_jelly",
        "name": "糖巧果冻",
        "sort": 4,
        "children": [
            {"id": 110, "key": "chocolate", "name": "巧克力", "sort": 1},
            {"id": 111, "key": "candy_gummy", "name": "糖果软糖", "sort": 2},
            {"id": 112, "key": "jelly_pudding", "name": "果冻布丁", "sort": 3},
        ],
    },
    {
        "id": 5,
        "key": "jerky_delicacies",
        "name": "肉脯卤味",
        "sort": 5,
        "children": [
            {"id": 113, "key": "jerky", "name": "肉干肉脯", "sort": 1},
            {"id": 114, "key": "braised_veggie", "name": "卤味素食", "sort": 2},
            {"id": 115, "key": "fish_tofu_seafood", "name": "鱼豆腐海味", "sort": 3},
        ],
    },
    {
        "id": 6,
        "key": "beverages",
        "name": "酒水饮料",
        "sort": 6,
        "children": [
            {"id": 116, "key": "soda", "name": "碳酸饮料", "sort": 1},
            {"id": 117, "key": "juice_tea", "name": "果汁茶饮", "sort": 2},
            {"id": 118, "key": "coffee_energy", "name": "咖啡功能饮料", "sort": 3},
            {"id": 119, "key": "water_sparkling", "name": "矿泉水苏打水", "sort": 4},
        ],
    },
    {
        "id": 7,
        "key": "dairy_mix",
        "name": "乳品冲饮",
        "sort": 7,
        "children": [
            {"id": 120, "key": "ambient_milk", "name": "常温乳品", "sort": 1},
            {"id": 121, "key": "yogurt_probiotic", "name": "酸奶乳酸菌", "sort": 2},
            {"id": 122, "key": "milk_tea_mix", "name": "奶茶冲饮", "sort": 3},
        ],
    },
    {
        "id": 8,
        "key": "specialty_gifts",
        "name": "特产礼盒",
        "sort": 8,
        "children": [
            {"id": 123, "key": "regional_specialty", "name": "地方特产", "sort": 1},
            {"id": 124, "key": "imported_snacks", "name": "进口零食", "sort": 2},
            {"id": 125, "key": "festival_gifts", "name": "节庆礼盒", "sort": 3},
        ],
    },
]


PRODUCT_ROWS = """
1|101|乐事|Lays|原味薯片|75g|7.90|Lay's Classic Potato Chips 75g|potato chips crispy snack
2|101|乐事|Lays|烧烤味薯片|70g|7.90|Lay's Barbecue Potato Chips 70g|potato chips crispy snack
3|101|乐事|Lays|海盐薯片|70g|8.50|Lay's Sea Salt Potato Chips 70g|potato chips crispy snack
4|101|品客|Pringles|原味薯片|110g|14.90|Pringles Original 110g|potato chips crispy snack
5|101|品客|Pringles|酸奶洋葱薯片|102g|14.90|Pringles Sour Cream Onion 102g|potato chips crispy snack
6|101|Ruffles|Ruffles|原味波纹薯片|70g|9.90|Ruffles Original Potato Chips 70g|potato chips crispy snack
7|101|奇多|Cheetos|香辣玉米脆|65g|8.90|Cheetos Crunchy Flamin Hot 65g|puffed corn snack
8|101|Kettle|Kettle|海盐黑胡椒厚切薯片|60g|15.90|Kettle Sea Salt Cracked Pepper Chips 60g|potato chips thick cut
9|102|旺旺|Want Want|仙贝|112g|12.80|Want Want Senbei Rice Crackers 112g|rice crackers crispy snack
10|102|旺旺|Want Want|雪饼|84g|9.90|Want Want Shelly Senbei 84g|rice crackers crispy snack
11|102|岩塚|Iwatsuka|海苔米饼|90g|15.80|Iwatsuka Seaweed Rice Crackers 90g|rice crackers seaweed snack
12|102|龟田|Kameda|柿种米果|100g|16.90|Kameda Kakinotane Rice Crackers 100g|rice crackers peanut mix
13|103|多力多滋|Doritos|芝士味玉米片|65g|11.90|Doritos Nacho Cheese 65g|corn chips snack
14|103|多力多滋|Doritos|甜辣味玉米片|65g|11.90|Doritos Sweet Chili 65g|corn chips snack
15|103|ACT II|ACT II|黄油爆米花|78g|12.90|ACT II Butter Popcorn 78g|butter popcorn snack
16|103|Smartfood|Smartfood|白切达爆米花|60g|13.90|Smartfood White Cheddar Popcorn 60g|popcorn snack cheese
17|104|奥利奥|Oreo|原味夹心饼干|97g|12.90|Oreo Original Sandwich Cookies 97g|sandwich cookies creme
18|104|奥利奥|Oreo|草莓味夹心饼干|97g|12.90|Oreo Strawberry Sandwich Cookies 97g|sandwich cookies strawberry
19|104|奥利奥|Oreo|巧克力味夹心饼干|97g|12.90|Oreo Chocolate Sandwich Cookies 97g|sandwich cookies chocolate
20|104|乐之|Ritz|芝士夹心苏打饼干|100g|10.90|Ritz Cheese Sandwich Crackers 100g|sandwich crackers cheese
21|104|乐之|Ritz|花生夹心苏打饼干|100g|10.90|Ritz Peanut Butter Sandwich Crackers 100g|sandwich crackers peanut
22|105|趣多多|Chips Ahoy|巧克力曲奇|95g|9.90|Chips Ahoy Chocolate Chip Cookies 95g|cookies wafer snack
23|105|丹麦蓝罐|Royal Dansk|曲奇|90g|22.90|Royal Dansk Butter Cookies 90g|butter cookies gift tin
24|105|莱家|Loacker|榛子威化|45g|13.80|Loacker Hazelnut Wafer 45g|wafer hazelnut snack
25|105|丽芝士纳宝帝|Richeese Nabati|奶酪威化|58g|8.90|Richeese Nabati Cheese Wafer 58g|wafer cheese snack
26|106|好丽友|Orion|经典巧克力派|204g|13.90|Orion Choco Pie 204g|cake pie chocolate
27|106|达利园|Daliyuan|法式小面包|200g|12.50|Daliyuan French Bread 200g|packaged bread snack
28|106|盼盼|Panpan|软华夫蛋糕|168g|14.80|Panpan Waffle Cake 168g|packaged cake waffle
29|106|宾堡|Bimbo|奶香吐司面包|180g|13.90|Bimbo Milk Bread 180g|milk bread package
30|106|桃李|Toly Bread|醇熟切片面包|300g|16.50|Toly Bread Sliced Bread 300g|sliced bread package
31|107|三只松鼠|Three Squirrels|每日坚果|175g|29.90|Three Squirrels Daily Nuts 175g|mixed nuts gift bag
32|107|沃隆|Wolong|每日坚果|150g|27.90|Wolong Daily Nuts 150g|mixed nuts gift bag
33|107|百草味|Be and Cheery|每日坚果|175g|28.90|Be and Cheery Daily Nuts 175g|mixed nuts gift bag
34|107|Planters|Planters|每日混合坚果|198g|32.90|Planters Mixed Nuts 198g|mixed nuts deluxe
35|108|洽洽|ChaCheer|原香瓜子|108g|7.50|ChaCheer Original Sunflower Seeds 108g|sunflower seeds roasted nuts
36|108|洽洽|ChaCheer|焦糖山核桃味瓜子|108g|8.50|ChaCheer Caramel Pecan Seeds 108g|sunflower seeds roasted nuts
37|108|三只松鼠|Three Squirrels|夏威夷果|200g|39.90|Three Squirrels Macadamia Nuts 200g|macadamia nuts snack
38|108|良品铺子|Bestore|巴旦木|180g|26.90|Bestore Almonds 180g|almonds nuts snack
39|108|百草味|Be and Cheery|开心果|190g|32.90|Be and Cheery Pistachios 190g|pistachios nuts snack
40|109|良品铺子|Bestore|芒果干|108g|18.90|Bestore Dried Mango 108g|dried fruit mango snack
41|109|百草味|Be and Cheery|草莓干|100g|19.90|Be and Cheery Dried Strawberry 100g|dried fruit strawberry snack
42|109|三只松鼠|Three Squirrels|蔓越莓干|120g|16.90|Three Squirrels Dried Cranberries 120g|dried fruit cranberry snack
43|109|溜溜梅|Liuliumei|清梅|88g|9.90|Liuliumei Preserved Plum 88g|preserved plum snack
44|109|良品铺子|Bestore|葡萄干|150g|13.90|Bestore Raisins 150g|raisins dried fruit snack
45|110|德芙|Dove|丝滑牛奶巧克力|90g|14.80|Dove Milk Chocolate 90g|chocolate candy package
46|110|费列罗|Ferrero|榛果威化巧克力|90g|39.90|Ferrero Rocher 90g|chocolate hazelnut gift
47|110|M&M's|M&Ms|花生牛奶巧克力豆|100g|13.90|M and M's Peanut Chocolate 100g|chocolate candy peanuts
48|110|士力架|Snickers|花生夹心巧克力|51g|6.90|Snickers Peanut Chocolate 51g|chocolate bar peanuts
49|111|阿尔卑斯|Alpenliebe|牛奶硬糖|84g|8.50|Alpenliebe Milk Candy 84g|candy gummy package
50|111|徐福记|Hsu Fu Chi|酥心糖|120g|16.80|Hsu Fu Chi Crisp Candy 120g|candy mixed package
51|111|Haribo|Haribo|金熊软糖|100g|12.90|Haribo Goldbears 100g|gummy candy bears
52|111|Skittles|Skittles|彩虹糖原果味|90g|11.90|Skittles Original 90g|fruit candy chewy
53|112|喜之郎|Cici|葡萄果冻|150g|7.90|Cici Jelly Grape 150g|jelly pudding snack cup
54|112|喜之郎|Cici|什锦果冻|200g|9.90|Cici Assorted Jelly 200g|jelly pudding snack cup
55|112|旺旺|Want Want|旺仔牛奶布丁|132g|10.90|Want Want Milk Pudding 132g|milk pudding jelly snack
56|112|蒟蒻|Konjac|白桃果冻|180g|12.90|Konjac Jelly White Peach 180g|konjac jelly fruit snack
57|113|良品铺子|Bestore|靖江猪肉脯|100g|19.90|Bestore Pork Jerky 100g|jerky snack package
58|113|三只松鼠|Three Squirrels|灯影牛肉丝|90g|22.90|Three Squirrels Shredded Beef Jerky 90g|beef jerky snack
59|113|来伊份|Laiyifen|手撕牛肉|88g|21.90|Laiyifen Hand Torn Beef 88g|beef jerky snack
60|113|棒棒娃|Bangbangwa|麻辣牛肉干|88g|18.90|Bangbangwa Spicy Beef Jerky 88g|beef jerky spicy snack
61|114|卫龙|Weilong|大面筋|106g|8.90|Weilong Spicy Gluten 106g|spicy gluten snack
62|114|良品铺子|Bestore|香卤兰花干|108g|10.90|Bestore Braised Tofu 108g|braised tofu spicy snack
63|114|盐津铺子|Yanjinpuzi|香辣豆干|108g|11.90|Yanjinpuzi Spicy Bean Curd 108g|braised tofu spicy snack
64|115|良品铺子|Bestore|鱼豆腐烧烤味|105g|12.90|Bestore Fish Tofu BBQ 105g|fish tofu seafood snack
65|115|三只松鼠|Three Squirrels|手撕鱿鱼条|80g|19.90|Three Squirrels Shredded Squid 80g|seafood squid snack
66|115|海欣|Haixin|鱼豆腐原味|120g|13.90|Haixin Fish Tofu Original 120g|fish tofu seafood snack
67|116|可口可乐|Coca Cola|可乐|330ml|3.50|Coca Cola 330ml|soda can beverage
68|116|百事|Pepsi|可乐|330ml|3.50|Pepsi Cola 330ml|soda can beverage
69|116|雪碧|Sprite|柠檬味汽水|330ml|3.50|Sprite Lemon Soda 330ml|soda can beverage
70|116|芬达|Fanta|橙味汽水|330ml|3.50|Fanta Orange 330ml|soda can beverage
71|117|康师傅|Master Kong|冰红茶|500ml|4.00|Master Kong Iced Tea 500ml|tea juice bottled drink
72|117|统一|Uni President|阿萨姆奶茶|500ml|5.50|Uni President Assam Milk Tea 500ml|tea juice bottled drink
73|117|农夫果园|Nongfu Orchard|混合果蔬汁|450ml|6.50|Nongfu Orchard Mixed Juice 450ml|juice bottled drink
74|117|维他|Vitasoy|柠檬茶|500ml|5.50|Vitasoy Lemon Tea 500ml|tea juice bottled drink
75|117|元气森林|Genki Forest|燃茶桃香乌龙|500ml|6.80|Genki Forest Peach Oolong Tea 500ml|tea juice bottled drink
76|117|美汁源|Minute Maid|果粒橙|450ml|4.80|Minute Maid Pulpy Orange 450ml|juice bottled drink
77|118|红牛|Red Bull|维生素风味饮料|250ml|6.50|Red Bull Energy Drink 250ml|energy drink bottled coffee
78|118|东鹏|Eastroc|特饮|250ml|5.80|Eastroc Super Drink 250ml|energy drink bottled coffee
79|118|雀巢|Nescafe|丝滑拿铁咖啡|268ml|7.90|Nescafe Latte 268ml|coffee bottled drink
80|119|农夫山泉|Nongfu Spring|饮用天然水|550ml|2.50|Nongfu Spring Water 550ml|sparkling water bottle
81|119|百岁山|Ganten|矿泉水|570ml|3.00|Ganten Mineral Water 570ml|mineral water bottle
82|119|巴黎水|Perrier|原味气泡水|330ml|5.90|Perrier Sparkling Water 330ml|sparkling water bottle
83|120|伊利|Yili|纯牛奶|250ml|4.50|Yili Pure Milk 250ml|milk carton soy milk package
84|120|蒙牛特仑苏|Telunsu|纯牛奶|250ml|5.80|Telunsu Pure Milk 250ml|milk carton soy milk package
85|120|维他奶|Vitasoy|原味豆奶|250ml|4.80|Vitasoy Original Soy Milk 250ml|soy milk drink carton
86|120|旺旺|Want Want|旺仔牛奶|245ml|5.50|Want Want Milk Drink 245ml|milk drink carton
87|121|养乐多|Yakult|乳酸菌饮品|100ml*5|13.90|Yakult Probiotic Drink 100ml x5|yogurt probiotic drink
88|121|蒙牛|Mengniu|优益C乳酸菌|340ml|7.90|Mengniu Yoyi C Probiotic Drink 340ml|yogurt probiotic drink
89|121|伊利|Yili|安慕希希腊风味酸奶|205g|8.90|Yili Ambrosial Yogurt 205g|yogurt probiotic drink
90|122|香飘飘|Xiangpiaopiao|原味奶茶|80g|11.90|Xiangpiaopiao Milk Tea 80g|instant milk tea powder pack
91|122|优乐美|Youlemei|珍珠奶茶|80g|10.90|Youlemei Bubble Milk Tea 80g|instant milk tea powder pack
92|122|立顿|Lipton|港式鸳鸯奶茶固体饮料|100g|14.90|Lipton Hong Kong Milk Tea 100g|instant milk tea powder pack
93|123|稻香村|Daoxiangcun|京八件糕点礼包|400g|39.90|Daoxiangcun Pastry Gift Pack 400g|regional specialty pastry snack
94|123|桂花|Osmanthus|绿豆糕|220g|16.90|Osmanthus Mung Bean Cake 220g|regional specialty pastry snack
95|123|重庆|Chongqing|怪味胡豆|180g|14.90|Chongqing Spicy Broad Beans 180g|regional specialty pastry snack
96|123|天津|Tianjin|麻花什锦装|360g|24.90|Tianjin Mahua Assorted 360g|regional specialty pastry snack
97|124|百乐滋|Pocky|巧克力棒|55g|9.90|Pocky Chocolate 55g|imported snack package
98|124|Loacker|Loacker|香草威化|125g|18.90|Loacker Vanilla Wafer 125g|imported snack package
99|125|三只松鼠|Three Squirrels|坚果礼盒|750g|89.00|Three Squirrels Nut Gift Box 750g|snack gift box package
100|125|德芙|Dove|巧克力礼盒|280g|79.00|Dove Chocolate Gift Box 280g|snack gift box package
""".strip().splitlines()


EXPIRE_DATE_MAP = {
    1: "2027-08-31",
    2: "2027-06-30",
    3: "2027-09-30",
    4: "2027-12-31",
    5: "2027-07-31",
    6: "2027-10-31",
    7: "2027-03-31",
    8: "2027-05-31",
}

PROMO_MAP = {
    1: "店长推荐",
    9: "米香热卖",
    13: "追剧搭子",
    17: "精选推荐",
    22: "下午茶搭子",
    26: "早餐优选",
    31: "营养搭子",
    35: "门店热卖",
    40: "酸甜开胃",
    45: "节日甜礼",
    49: "童年回味",
    53: "冰镇更佳",
    57: "越嚼越香",
    61: "香辣上头",
    64: "海味小食",
    67: "冰镇更爽",
    71: "解腻推荐",
    77: "提神必备",
    80: "清爽补水",
    83: "早餐常备",
    87: "冷藏更佳",
    90: "冲泡方便",
    93: "地方风味",
    97: "进口人气",
    99: "送礼优选",
}

EXCLUDE_TERMS_MAP = {
    101: ["soup", "broth", "ham", "tuna", "vegetable", "legumes", "chicoree"],
    102: ["ham", "soup", "charcuterie", "tuna"],
    103: ["ham", "soup", "charcuterie", "tuna"],
    104: ["ham", "soup", "charcuterie"],
    105: ["ham", "soup", "charcuterie"],
    106: ["ham", "soup", "charcuterie"],
    107: ["ham", "tuna", "soup"],
    108: ["ham", "tuna", "soup"],
    109: ["ham", "tuna", "soup"],
    110: ["ham", "tuna", "soup"],
    111: ["ham", "tuna", "soup"],
    112: ["ham", "tuna", "soup"],
    113: ["soup", "toothpaste", "detergent", "vegetable"],
    114: ["soup", "toothpaste", "detergent", "vegetable"],
    115: ["soup", "toothpaste", "detergent", "vegetable"],
    116: ["ham", "soup", "detergent"],
    117: ["ham", "soup", "detergent"],
    118: ["ham", "soup", "detergent"],
    119: ["ham", "soup", "detergent"],
    120: ["ham", "soup", "detergent"],
    121: ["ham", "soup", "detergent"],
    122: ["ham", "soup", "detergent"],
    123: ["ham", "soup", "detergent"],
    124: ["ham", "soup", "detergent"],
    125: ["ham", "soup", "detergent"],
}

DESCRIPTION_MAP = {
    101: "{brand}{short_name}采用{spec}便携规格，薯香浓郁、口感酥脆，追剧分享和办公室解馋都很合适。",
    102: "{brand}{short_name}带来扎实米香与轻脆口感，{spec}分享装越嚼越香，适合下午茶和家庭零嘴。",
    103: "{brand}{short_name}主打玉米谷物香气，{spec}独立装方便囤货，看片、露营和聚会都很受欢迎。",
    104: "{brand}{short_name}夹心细腻、甜而不腻，{spec}刚好适合随手开袋，搭配牛奶咖啡更有满足感。",
    105: "{brand}{short_name}奶香和酥脆层次分明，{spec}装随手带着走，是下午茶和办公室分享的高频选择。",
    106: "{brand}{short_name}口感松软细腻，{spec}规格适合早餐、加班垫肚子或下午茶轻松补能量。",
    107: "{brand}{short_name}甄选多种果仁组合，{spec}随手包设计方便补充能量，通勤、办公和健身后都适合。",
    108: "{brand}{short_name}果仁饱满、新鲜香脆，{spec}分享装越嚼越香，是门店里很受欢迎的耐吃型零嘴。",
    109: "{brand}{short_name}果香自然、酸甜开胃，{spec}小包装方便囤货，追剧和旅途路上都能随手来一包。",
    110: "{brand}{short_name}口感丝滑香浓，{spec}规格兼顾分享与自留，节日备糖和日常解馋都很体面。",
    111: "{brand}{short_name}甜味讨喜、入口轻松，{spec}包装适合家庭囤货和朋友聚会，气氛感很足。",
    112: "{brand}{short_name}Q弹顺滑、果味清爽，{spec}冷藏后口感更佳，下午茶和家庭餐后都很合适。",
    113: "{brand}{short_name}肉香扎实、耐嚼过瘾，{spec}独立装方便分享，越嚼越香很适合深夜解馋。",
    114: "{brand}{short_name}卤香入味、层次丰富，{spec}规格方便随手开吃，微辣开胃是很多老客的回购款。",
    115: "{brand}{short_name}海味鲜香、Q弹有嚼劲，{spec}分享装适合露营、追剧和办公室囤零嘴。",
    116: "{brand}{short_name}冰镇后更加畅爽，{spec}单罐刚好满足一顿快乐补给，搭配炸物和薯片特别对味。",
    117: "{brand}{short_name}主打清爽茶果风味，{spec}瓶装带着走更方便，解腻顺口，适合日常补水。",
    118: "{brand}{short_name}兼顾风味和提神体验，{spec}方便冷藏即饮，通勤、加班和长途出行都很实用。",
    119: "{brand}{short_name}口感清冽干净，{spec}即开即饮，随餐搭配不抢味，是日常补水的安心选择。",
    120: "{brand}{short_name}奶香顺滑、入口自然，{spec}规格适合早餐、下午茶和居家常备。",
    121: "{brand}{short_name}酸甜清爽、口感轻盈，{spec}冷藏后风味更好，是很多用户的早餐搭档。",
    122: "{brand}{short_name}冲泡方便、奶香浓郁，{spec}规格刚好满足一杯热饮需求，下午茶氛围感拉满。",
    123: "{brand}{short_name}带有鲜明地方风味，{spec}包装拿得出手，自己吃或待客分享都很合适。",
    124: "{brand}{short_name}是门店里很稳定的人气进口零食，{spec}包装精致，送朋友也很加分。",
    125: "{brand}{short_name}更适合节庆送礼和多人分享，{spec}礼盒装体面大方，囤年货也很省心。",
}

STOP_TOKENS = {
    "and",
    "the",
    "with",
    "for",
    "snack",
    "package",
    "pack",
    "drink",
    "bottle",
    "original",
    "classic",
    "flavor",
    "flavour",
    "s",
}


def tokenize(text: str) -> list[str]:
    return re.findall(r"[a-z0-9]+|[\u4e00-\u9fff]{2,}", (text or "").lower())


def unique(values: list[str]) -> list[str]:
    output: list[str] = []
    seen: set[str] = set()
    for value in values:
        if value and value not in seen:
            seen.add(value)
            output.append(value)
    return output


def build_maps() -> tuple[dict[int, dict], dict[int, dict]]:
    child_map: dict[int, dict] = {}
    parent_map: dict[int, dict] = {}
    for category in CATEGORIES:
        for child in category["children"]:
            child_map[child["id"]] = child
            parent_map[child["id"]] = category
    return child_map, parent_map


def build_products() -> list[dict]:
    child_map, parent_map = build_maps()
    products: list[dict] = []

    for line in PRODUCT_ROWS:
        (
            goods_id_str,
            category_id_str,
            brand,
            brand_en,
            short_name,
            spec,
            price_str,
            query_en,
            fallback_terms,
        ) = line.split("|")
        goods_id = int(goods_id_str)
        category_id = int(category_id_str)
        category = child_map[category_id]
        parent = parent_map[category_id]
        name = f"{brand}{short_name} {spec}"
        image_url = f"/uploads/goods/goods_{goods_id:03d}.jpg"
        description = DESCRIPTION_MAP[category_id].format(
            brand=brand,
            short_name=short_name,
            spec=spec,
        )

        query_base = re.sub(
            r"\b\d+(?:g|ml)(?:\s*x\s*\d+)?\b",
            "",
            query_en,
            flags=re.IGNORECASE,
        ).strip()
        query_base = re.sub(r"\s{2,}", " ", query_base)

        search_terms = unique(
            [
                query_en,
                query_base,
                f"{brand_en} {short_name} {spec}",
                f"{brand} {short_name} {spec}",
            ]
        )
        brand_terms = [token for token in tokenize(brand_en) if token not in STOP_TOKENS]
        core_terms = [
            token
            for token in tokenize(query_base or query_en)
            if token not in STOP_TOKENS and token not in brand_terms
        ]
        if not core_terms:
            core_terms = [token for token in tokenize(fallback_terms) if token not in STOP_TOKENS]

        products.append(
            {
                "id": goods_id,
                "category_key": category["key"],
                "category_id": category_id,
                "category_name": category["name"],
                "parent_category_key": parent["key"],
                "parent_category_name": parent["name"],
                "brand": brand,
                "short_name": short_name,
                "spec": spec,
                "name": name,
                "price": float(price_str),
                "stock": 36 + (goods_id * 7) % 65,
                "safety_stock": 8 + (goods_id % 9),
                "bar_code": f"6909{goods_id:09d}",
                "image_url": image_url,
                "file_name": f"goods_{goods_id:03d}.jpg",
                "description": description,
                "promotion_tag": PROMO_MAP.get(goods_id, ""),
                "is_on_sale": 1,
                "expire_date": EXPIRE_DATE_MAP[parent["id"]],
                "search_terms": search_terms,
                "fallback_terms": fallback_terms,
                "image_brand_terms": brand_terms,
                "image_core_terms": unique(core_terms[:6]),
                "image_exclude_terms": EXCLUDE_TERMS_MAP[category_id],
                "image_override_urls": [],
            }
        )

    return products


def main() -> None:
    seed = {
        "project_name": "LeYi零食店",
        "image_base_path": "/uploads/goods",
        "categories": CATEGORIES,
        "products": build_products(),
    }
    SEED_PATH.write_text(json.dumps(seed, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"seed rebuilt: {len(seed['products'])} products -> {SEED_PATH}")


if __name__ == "__main__":
    main()
