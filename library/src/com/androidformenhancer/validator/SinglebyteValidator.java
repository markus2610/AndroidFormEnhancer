/*
 * Copyright 2012 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidformenhancer.validator;

import com.androidformenhancer.R;
import com.androidformenhancer.form.annotation.Singlebyte;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

/**
 * Validates that the value of the field consists of single-byte characters or
 * not.
 * 
 * @author Soichiro Kashima
 */
public class SinglebyteValidator extends Validator {

    private static final String TAG = "SinglebyteValidator";
    private static final String DEFAULT_ENCODING = "UTF-8";

    private String mEncoding;

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        setEncoding();
    }

    @Override
    public String validate(final Field field) {
        Object value;
        try {
            value = field.get(getTarget());
        } catch (Exception e) {
            // TODO Throw some exception to inform caller this illegal state
            Log.v(TAG, e.getMessage());
            return null;
        }

        Singlebyte singlebyte = field.getAnnotation(Singlebyte.class);
        if (singlebyte != null) {
            final Class<?> type = field.getType();
            if (type.equals(String.class)) {
                final String strValue = (String) value;
                if (TextUtils.isEmpty(strValue)) {
                    return null;
                }
                boolean hasError = false;
                try {
                    for (int i = 0; i < strValue.length(); i = strValue.offsetByCodePoints(i, 1)) {
                        char[] c = Character.toChars(strValue.codePointAt(i));
                        if (c.length > 1) {
                            hasError = true;
                            break;
                        }
                        byte[] b = new String(c).getBytes(mEncoding);
                        if (b.length != 1) {
                            hasError = true;
                            break;
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new ValidationException("Unsupported encoding used: " + mEncoding, e);
                }
                if (hasError) {
                    String name = field.getName();
                    int nameResId = getNameResourceId(field);
                    if (nameResId > 0) {
                        name = getContext().getResources().getString(nameResId);
                    }
                    nameResId = singlebyte.nameResId();
                    if (nameResId > 0) {
                        name = getContext().getResources().getString(nameResId);
                    }
                    Object[] messageParams = new Object[] {
                            name
                    };
                    return getMessage(R.styleable.ValidatorMessages_afeErrorSinglebyte,
                            R.string.afe__msg_validation_singlebyte,
                            messageParams);
                }
            }
        }

        return null;
    }

    public String getEncoding() {
        return mEncoding;
    }

    public void setEncoding(final String encoding) {
        mEncoding = encoding;
    }

    private void setEncoding() {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(null,
                R.styleable.ValidatorDefinitions,
                R.attr.afeValidatorDefinitions, 0);

        mEncoding = a.getString(R.styleable.ValidatorDefinitions_afeCharacterEncoding);
        if (TextUtils.isEmpty(mEncoding)) {
            mEncoding = DEFAULT_ENCODING;
        }

        a.recycle();
    }

}
