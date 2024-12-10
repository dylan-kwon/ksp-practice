package dylan.kwon.kotlin.ksp.practice.repository

import dylan.kwon.kotlin.ksp.myannotation.HiltBinds
import javax.inject.Inject

@HiltBinds(isSingleton = true)
class RealRepository @Inject constructor() : Repository {

    override fun get(): String {
        return "This is RealRepository"
    }

}