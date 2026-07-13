variable "aws_region" {
  description = "AWS region to deploy the cluster into"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "product-management"
}

variable "kubernetes_version" {
  description = "Kubernetes version for the EKS control plane"
  type        = string
  default     = "1.33"
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "node_instance_types" {
  description = "Instance types for the EKS managed node group — must stay free-tier-eligible (verify with `aws ec2 describe-instance-types --filters Name=free-tier-eligible,Values=true`) since this account is on AWS's restrictive Free Plan, which hard-rejects launches of anything else"
  type        = list(string)
  default     = ["t3.small"]
}

variable "node_min_size" {
  description = "Minimum number of nodes in the managed node group"
  type        = number
  default     = 1
}

variable "node_max_size" {
  description = "Maximum number of nodes in the managed node group"
  type        = number
  default     = 2
}

variable "node_desired_size" {
  description = "Desired number of nodes in the managed node group (ignored after initial creation)"
  type        = number
  default     = 2
}

variable "tags" {
  description = "Common tags applied to all resources"
  type        = map(string)
  default = {
    Project     = "quarkus-react"
    Environment = "portfolio"
    ManagedBy   = "terraform"
  }
}
