RULE trace main entry
CLASS ${data.clazzName}
METHOD ${methodName}
HELPER com.cte4.mac.machelper.rules.function.UsageFuncHelper
AT ENTRY
IF TRUE
DO createTimer($0)
ENDRULE

RULE trace main exit
CLASS ${data.clazzName}
METHOD ${methodName}
HELPER com.cte4.mac.machelper.rules.function.UsageFuncHelper
AT EXIT
BIND ts : long = getElapsedTimeFromTimer($0)
IF TRUE
DO sendMetrics($CLASS, $METHOD, ts);
   deleteTimer($0)
ENDRULE