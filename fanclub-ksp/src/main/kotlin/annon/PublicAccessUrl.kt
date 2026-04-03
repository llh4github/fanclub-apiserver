package llh.fanclubvup.ksp.annon

/**
 * URL 允许所有人访问
 * 仅能用于类的成员方法
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class PublicAccessUrl() {

}
