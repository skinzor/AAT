package ch.bailu.aat.services.background;

import java.io.Closeable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import ch.bailu.aat.helpers.AppBroadcaster;
import ch.bailu.aat.helpers.AppIntent;
import ch.bailu.aat.helpers.AppLog;
import ch.bailu.aat.services.ServiceContext;
import ch.bailu.aat.services.VirtualService;

public class BackgroundService extends VirtualService {


    private final Self self;

    public Self getSelf() {
        return self;
    }


    public BackgroundService(ServiceContext sc) {
        super(sc);
        self = new SelfOn();
    }


    
    public void close() {
        self.close();
    }


    @Override
    public void appendStatusText(StringBuilder builder) {
        self.appendStatusText(builder);
    }


    
    public static class Self implements Closeable {
        public void process(ProcessHandle handle) {}
        public void download(ProcessHandle handle) {}
        public void load(ProcessHandle handle) {}
        public void downloadMapFeatures() {}
        @Override
        public void close() {}
        public void appendStatusText(StringBuilder builder) {}
    }


    
    public class SelfOn extends Self {
        private final static int PROCESS_QUEUE_SIZE=500;

        private final SparseArray<DownloaderThread> downloaders = new SparseArray<DownloaderThread>();
        private final SparseArray<LoaderThread> loaders = new SparseArray<LoaderThread>();
        private ProcessThread process;


        private MapFeaturesDownloader mapFeaturesDownloader;

        private BroadcastReceiver onFileDownloaded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AppLog.i(context, AppIntent.getFile(intent));
            }
        };

        public SelfOn() {
            mapFeaturesDownloader = new MapFeaturesDownloader(getSContext());
            AppBroadcaster.register(getContext(), onFileDownloaded, AppBroadcaster.FILE_CHANGED_ONDISK);


            process =new ProcessThread(PROCESS_QUEUE_SIZE) {

                @Override
                public void bgOnHaveHandle(ProcessHandle handle) {
                    if (handle.canContinue()) {
                        handle.bgLock();
                        handle.bgOnProcess();
                        handle.bgUnlock();
                        handle.broadcast(getContext());

                    }
                }
            };

        }
        @Override
        public void process(ProcessHandle handle) {
            process.process(handle);
        }

        @Override
        public void download(ProcessHandle handle) {
            URL url;
            try {
                url = new URL(handle.toString());
            } catch (MalformedURLException e) {
                url = null;
            }

            if (url != null) {
                String host = url.getHost();
                DownloaderThread downloader = downloaders.get(host.hashCode());

                if (downloader == null) {
                    downloader = new DownloaderThread(getContext(), host);
                    downloaders.put(host.hashCode(), downloader);
                }
                downloader.process(handle);
            }
        }


        @Override
        public void load(ProcessHandle handle) {
            final String base = getBaseDirectory(handle.toString());

            LoaderThread loader = loaders.get(base.hashCode());

            if (loader == null) {
                loader = new LoaderThread(getContext(), base);
                loaders.put(base.hashCode(), loader);
            }
            loader.process(handle);
        }

        @Override
        public void downloadMapFeatures() {
            mapFeaturesDownloader.download();
        }
        @Override
        public void close() {
            getContext().unregisterReceiver(onFileDownloaded);

            mapFeaturesDownloader.close();

            for (int i=0; i<loaders.size(); i++)
                loaders.valueAt(i).close();
            loaders.clear();

            for (int i=0; i<downloaders.size(); i++)
                downloaders.valueAt(i).close();
            downloaders.clear();

            process.close();
            process=null;
        }

        private String getBaseDirectory(String id) {
            File p1 = new File (id);
            File r = p1;

            int c=0;
            final int t=3;

            while (p1!=null) {
                p1=p1.getParentFile();

                if (c<t) {
                    c++;

                } else {
                    r=r.getParentFile();

                }

            }

            return r.getAbsolutePath();
        }

        @Override        
        public void appendStatusText(StringBuilder builder) {

            for (int i=0; i<loaders.size(); i++)
                loaders.valueAt(i).appendStatusText(builder);

            for (int i=0; i<downloaders.size(); i++)
                downloaders.valueAt(i).appendStatusText(builder);
        }
    }
}