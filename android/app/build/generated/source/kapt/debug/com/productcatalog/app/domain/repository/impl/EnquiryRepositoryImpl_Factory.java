package com.productcatalog.app.domain.repository.impl;

import com.productcatalog.app.data.remote.EnquiryApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class EnquiryRepositoryImpl_Factory implements Factory<EnquiryRepositoryImpl> {
  private final Provider<EnquiryApi> apiProvider;

  public EnquiryRepositoryImpl_Factory(Provider<EnquiryApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public EnquiryRepositoryImpl get() {
    return newInstance(apiProvider.get());
  }

  public static EnquiryRepositoryImpl_Factory create(Provider<EnquiryApi> apiProvider) {
    return new EnquiryRepositoryImpl_Factory(apiProvider);
  }

  public static EnquiryRepositoryImpl newInstance(EnquiryApi api) {
    return new EnquiryRepositoryImpl(api);
  }
}
