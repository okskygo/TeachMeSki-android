package com.teachmeski.app.di

import com.teachmeski.app.data.repository.AuthRepositoryImpl
import com.teachmeski.app.data.repository.BlockRepositoryImpl
import com.teachmeski.app.data.repository.ChatRepositoryImpl
import com.teachmeski.app.data.repository.ContactRepositoryImpl
import com.teachmeski.app.data.repository.ExploreRepositoryImpl
import com.teachmeski.app.data.repository.InstructorRepositoryImpl
import com.teachmeski.app.data.repository.LessonRequestRepositoryImpl
import com.teachmeski.app.data.repository.ResortRepositoryImpl
import com.teachmeski.app.data.repository.UserRepositoryImpl
import com.teachmeski.app.data.repository.PhoneVerificationRepositoryImpl
import com.teachmeski.app.data.repository.ReportRepositoryImpl
import com.teachmeski.app.data.repository.ReviewRepositoryImpl
import com.teachmeski.app.data.repository.WalletRepositoryImpl
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.BlockRepository
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.domain.repository.ContactRepository
import com.teachmeski.app.domain.repository.ExploreRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.LessonRequestRepository
import com.teachmeski.app.domain.repository.ResortRepository
import com.teachmeski.app.domain.repository.UserRepository
import com.teachmeski.app.domain.repository.PhoneVerificationRepository
import com.teachmeski.app.domain.repository.ReportRepository
import com.teachmeski.app.domain.repository.ReviewRepository
import com.teachmeski.app.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindResortRepository(impl: ResortRepositoryImpl): ResortRepository

    @Binds
    @Singleton
    abstract fun bindLessonRequestRepository(impl: LessonRequestRepositoryImpl): LessonRequestRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindInstructorRepository(impl: InstructorRepositoryImpl): InstructorRepository

    @Binds
    @Singleton
    abstract fun bindExploreRepository(impl: ExploreRepositoryImpl): ExploreRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindPhoneVerificationRepository(
        impl: PhoneVerificationRepositoryImpl,
    ): PhoneVerificationRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindBlockRepository(impl: BlockRepositoryImpl): BlockRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds
    @Singleton
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): ReviewRepository
}
