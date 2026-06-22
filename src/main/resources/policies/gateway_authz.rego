package authz

import future.keywords.in

default allow = false
default reason = "Denied by default policy"

allow {
    some role in input.claims.roles
    role_permissions := data.roles[role]
    some perm in role_permissions
    
    method_matches(perm.method, input.method)
    path_matches(perm.path, input.raw_path)
}

allow {
    # Allow super admins to access everything
    some role in input.claims.roles
    role == "SUPER_ADMIN"
}

reason = "Authorized" {
    allow
}

method_matches(perm_method, input_method) {
    perm_method == "*"
}
method_matches(perm_method, input_method) {
    perm_method == input_method
}

path_matches(perm_path, input_path) {
    perm_path == "*"
}
path_matches(perm_path, input_path) {
    perm_path == input_path
}
path_matches(perm_path, input_path) {
    endswith(perm_path, "/*")
    prefix := trim_suffix(perm_path, "/*")
    startswith(input_path, prefix)
}
