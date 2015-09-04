package com.eftimoff.idcardreader.ui.choose;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.eftimoff.idcardreader.R;
import com.eftimoff.idcardreader.components.passport.DaggerPassportComponent;
import com.eftimoff.idcardreader.components.passport.PassportModule;
import com.eftimoff.idcardreader.components.passport.service.PassportService;
import com.eftimoff.idcardreader.models.IdCard;
import com.eftimoff.idcardreader.models.Passport;
import com.eftimoff.idcardreader.models.PassportType;
import com.eftimoff.idcardreader.ui.camera.ShowCameraFragment;
import com.eftimoff.idcardreader.ui.common.BaseActivity;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Observer;

public class OcrActivity extends BaseActivity implements ChooseFragment.ChooseFragmentDelegate, ShowCameraFragment.ShowCameraFragmentDelegate {

    private static final String EXTRA_SHOULD_SKIP_CHOOSE = "extra_should_skip_choose";
    private static final String EXTRA_SKIP_CHOOSE_TYPE = "extra_skip_choose_type";

    @Override
    protected int layoutResourceId() {
        return R.layout.activity_choose;
    }

    @Override
    protected int containerId() {
        return R.id.container;
    }

    @Override
    protected void init() {
        final boolean shouldChooseSkipStep = getIntent().getBooleanExtra(EXTRA_SKIP_CHOOSE_TYPE, false);
        if (shouldChooseSkipStep) {
            final PassportModule passportModule = new PassportModule(this);
            final PassportService passportService = DaggerPassportComponent.builder().passportModule(passportModule).build().provideCountryService();
            final Observable<List<Passport>> passportsObservable = passportService.getPassports();
            passportsObservable.subscribe(observer);
            return;
        }
        startFragment(ChooseFragment.getInstance());
    }

    @Override
    public void onChoose(final Passport passport) {
        startFragment(ShowCameraFragment.getInstance(passport));
    }

    @Override
    public void onFinish(final IdCard idCard) {
        Toast.makeText(this, new Date(idCard.getDateOfBirth() * 1000).toString(), Toast.LENGTH_LONG).show();
    }

    public static Builder buildIntent() {
        return new Builder();
    }

    public static class Builder {

        private PassportType passportType;

        public Builder skipChooseStep(final PassportType passportType) {
            this.passportType = passportType;
            return this;
        }

        public Intent build(final Context context) {
            final Intent intent = new Intent(context, OcrActivity.class);
            intent.putExtra(EXTRA_SHOULD_SKIP_CHOOSE, passportType != null);
            intent.putExtra(EXTRA_SKIP_CHOOSE_TYPE, passportType);
            return intent;
        }
    }

    private final Observer<List<Passport>> observer = new Observer<List<Passport>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(final Throwable e) {

        }

        @Override
        public void onNext(final List<Passport> passports) {
            for (final Passport passport : passports) {
                final PassportType passportType = (PassportType) getIntent().getSerializableExtra(EXTRA_SKIP_CHOOSE_TYPE);
                if (passport.getType().ordinal() == passportType.ordinal()) {
                    onChoose(passport);
                    return;
                }
            }

        }
    };
}