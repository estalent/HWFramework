package com.nxp.intf;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INxpExtrasService extends IInterface {

    public static abstract class Stub extends Binder implements INxpExtrasService {
        private static final String DESCRIPTOR = "com.nxp.intf.INxpExtrasService";
        static final int TRANSACTION_close = 2;
        static final int TRANSACTION_getSecureElementUid = 4;
        static final int TRANSACTION_isEnabled = 5;
        static final int TRANSACTION_open = 1;
        static final int TRANSACTION_transceive = 3;

        private static class Proxy implements INxpExtrasService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public Bundle open(String pkg, IBinder b) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    this.mRemote.transact(Stub.TRANSACTION_open, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle close(String pkg, IBinder b) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStrongBinder(b);
                    this.mRemote.transact(Stub.TRANSACTION_close, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle transceive(String pkg, byte[] data_in) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeByteArray(data_in);
                    this.mRemote.transact(Stub.TRANSACTION_transceive, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getSecureElementUid(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    this.mRemote.transact(Stub.TRANSACTION_getSecureElementUid, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpExtrasService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpExtrasService)) {
                return new Proxy(obj);
            }
            return (INxpExtrasService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            Bundle _result;
            switch (code) {
                case TRANSACTION_open /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = open(data.readString(), data.readStrongBinder());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_open);
                        _result.writeToParcel(reply, TRANSACTION_open);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_close /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = close(data.readString(), data.readStrongBinder());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_open);
                        _result.writeToParcel(reply, TRANSACTION_open);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_transceive /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = transceive(data.readString(), data.createByteArray());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_open);
                        _result.writeToParcel(reply, TRANSACTION_open);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_getSecureElementUid /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result2 = getSecureElementUid(data.readString());
                    reply.writeNoException();
                    reply.writeByteArray(_result2);
                    return true;
                case TRANSACTION_isEnabled /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = isEnabled();
                    reply.writeNoException();
                    if (_result3) {
                        i = TRANSACTION_open;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Bundle close(String str, IBinder iBinder) throws RemoteException;

    byte[] getSecureElementUid(String str) throws RemoteException;

    boolean isEnabled() throws RemoteException;

    Bundle open(String str, IBinder iBinder) throws RemoteException;

    Bundle transceive(String str, byte[] bArr) throws RemoteException;
}
