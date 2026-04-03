---
name: 生成建表语句
description: 根据Jimmer框架的Kotlin实体类生成对应的建表语句
---

1. 生成语法要符合PGSQL语法规范与推荐用法。
2. 表主键类型为：bigint 不是自增的主键，主键值由程序生成。
3. 根据 Schema 注解填充表注释、字段注释。简短说明即可，可以忽略注解中example的内容。
4. 有Column注解指定字段别名时DDL中使用其值。
5. 有org.babyfish.jimmer.sql.Key的字段为唯一索引字段。
6. 生成日期类型字段时，默认使用 TIMESTAMP 类型，毫秒精度
    1. created_time 字段默认当前时间
    2. updated_time 字段默认当前时间，创建触发器以自动更新
7. 根据kotlin语言的可空性决定对应字段是否可空
8. PGSQL的索引名加上表名前缀，避免冲突
9. 对于roomId 、 BID 字段需要创建check约束：非负。
