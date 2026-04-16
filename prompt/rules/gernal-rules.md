1. ORM库使用文档地址：https://babyfish-ct.github.io/jimmer-doc/zh/docs/overview/welcome/
2. 代码使用 kotlin 官方推荐的语法，包括但不限于：
    1. 类、函数、属性等使用驼峰命名法
    2. 常量使用全大写命名法
    3. 空安全操作符（?.、?:）的使用
    4. 集合操作符（map、filter、reduce 等）的使用
    5. 数据类的使用
    6. 函数式编程的使用
3. HTTP接口出入参数类使用 Schema 注解标注信息，至少包含 title，description 信息
4. HTTP接口出参使用JsonWrapper类包装
5. serive包下的类返回值不能使用JsonWrapper
6. serive包使用接口和其实现类的方式组织代码
7. 新增非springboot依赖时，先在libs.versions.toml文件中定义，再在build.gradle.kts文件中引入
