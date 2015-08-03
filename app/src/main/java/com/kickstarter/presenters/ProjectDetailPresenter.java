package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClient;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.activities.ProjectDetailActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class ProjectDetailPresenter extends Presenter<ProjectDetailActivity> {
  @Inject ApiClient client;
  private final PublishSubject<Void> backProjectClick = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KsrApplication) context.getApplicationContext()).component().inject(this);
  }

  public void takeProject(final Project project) {
    final Observable<Project> latestProject = client.fetchProject(project).startWith(project);

    addSubscription(RxUtils.combineLatestPair(latestProject, viewSubject)
      .filter(v -> v.second != null)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> v.second.show(v.first)));

    addSubscription(RxUtils.combineLatestPair(latestProject, backProjectClick)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(v -> back(v.first)));
  }

  public void takeBackProjectClick() {
    backProjectClick.onNext(null);
  }

  protected void back(final Project project) {
    final Intent intent = new Intent(view(), CheckoutActivity.class);
    intent.putExtra("project", project);
    intent.putExtra("url", project.newPledgeUrl());
    view().startActivity(intent);
    view().overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
