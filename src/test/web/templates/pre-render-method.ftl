[#ftl/]
${jsonCalled?then("JSON_Yep!", "JSON_Nope!")}
${forwardCalled?then("Forward_Yep!", "Forward_Nope!")}
${noopCalled?then("Noop_Yep!", "Noop_Nope!")}