package pl.project13.log;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import pl.project13.core.log.LogInterface;

public interface CombinedLogger extends Logger, LogInterface, Log {

}
