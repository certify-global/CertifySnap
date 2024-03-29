/***
 Copyright (c) 2017 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain	a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.certify.snap.database.secureDB;

import android.util.SparseArray;
import androidx.sqlite.db.SupportSQLiteProgram;

class BindingsRecorder implements SupportSQLiteProgram {
  private SparseArray<Object> bindings=new SparseArray<>();

  @Override
  public void bindNull(int index) {
    bindings.put(index, null);
  }

  @Override
  public void bindLong(int index, long value) {
    bindings.put(index, value);
  }

  @Override
  public void bindDouble(int index, double value) {
    bindings.put(index, value);
  }

  @Override
  public void bindString(int index, String value) {
    bindings.put(index, value);
  }

  @Override
  public void bindBlob(int index, byte[] value) {
    bindings.put(index, value);
  }

  @Override
  public void clearBindings() {
    bindings.clear();
  }

  @Override
  public void close() {
    clearBindings();
  }

  String[] getBindings() {
    final String[] result=new String[bindings.size()];

    for (int i=0;i<bindings.size();i++) {
      int key=bindings.keyAt(i);
      Object binding=bindings.get(key);

      if (binding!=null) {
        result[i]=bindings.get(key).toString();
      }
      else {
        result[i]=""; // SQLCipher does not like null binding values
      }
    }

    return(result);
  }
}
